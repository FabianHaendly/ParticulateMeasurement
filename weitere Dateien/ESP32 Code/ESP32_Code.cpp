#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
byte SET_WORK_PERIOD[19] = {
  0xAA,
  0xB4,
  0x08,
  0x01,
  0x00,   
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0x00,
  0xFF,
  0xFF,
  0x07,
  0xAB
};


BLEServer *pServer = NULL;
BLECharacteristic *pCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// sensor variables
char sds_buffer[10];
byte buffer_pos_w = 0;

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

class MyServerCallbacks : public BLEServerCallbacks
{
    void onConnect(BLEServer *pServer)
    {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer *pServer)
    {
      deviceConnected = false;
    }
};

void setup()
{
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("MC ESP32");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_NOTIFY);

  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  Serial.println("Waiting for a client connection to notify...");

  // PIN 16 = RX, PIN 17 = TX
  Serial2.begin(9600, SERIAL_8N1, 16, 17);
  for (uint8_t i = 0; i < 19; i++) {
    Serial2.write(SET_WORK_PERIOD[i]);
  }

  clearBuffer();
}

void loop()
{
  float pm25 = 0.0;
  float pm10 = 0.0;
  unsigned int devId = 0;
  byte checkSum = 0;

  if (Serial2.available())
  {
    sds_buffer[buffer_pos_w] = Serial2.read();

    //Check if Startbyte is 0xAA
    if (sds_buffer[0] != 0xAA)
    {
      buffer_pos_w = 0;
      return;
    }
    buffer_pos_w++;

    // To set remaining 9 Bytes (see datasheet of sds011)
    if (buffer_pos_w < 10)
    {
      return;
    }

    // Endbyte must be 0xAB; else start all over again
    if (sds_buffer[9] != 0xAB)
    {
      buffer_pos_w = 0;
      sds_buffer[0] = 0x00;
      return;
    }

    if (sds_buffer[1] != 0xC0)
    {
      clearBuffer();
      return;
    }

    pm10 = (sds_buffer[4] + (sds_buffer[5] << 8)) / 10.0;
    pm25 = (sds_buffer[2] + (sds_buffer[3] << 8)) / 10.0;
    devId = (sds_buffer[6] + (sds_buffer[7] << 8));

    for (buffer_pos_w = 2; buffer_pos_w < 8; buffer_pos_w++)
    {
      checkSum += sds_buffer[buffer_pos_w];
    }

    if (checkSum != sds_buffer[8])
    {
      //@TODO Mysterious error
      clearBuffer();
      return;
    }
  }

  std::string sensorValue = returnSensorData(pm10, pm25, devId);

  // notify changed value
  if (deviceConnected && sensorValue != "Incomplete")
  {
    pCharacteristic->setValue(sensorValue);
    pCharacteristic->notify();
    delay(100); // bluetooth stack will go into congestion, if too many packets are sent
  }
  // disconnecting
  if (!deviceConnected && oldDeviceConnected)
  {
    delay(500);          // give the bluetooth stack the chance to get things ready
    pServer->startAdvertising(); // restart advertising
    Serial.println("start advertising");
    oldDeviceConnected = deviceConnected;
  }
  if (deviceConnected && !oldDeviceConnected)
  {
    oldDeviceConnected = deviceConnected;
  }

  clearBuffer();
}

void clearBuffer()
{
  for (byte buffer_pos_w = 0; buffer_pos_w < 10; buffer_pos_w++)
  {
    sds_buffer[buffer_pos_w] = 0x00;
  }
}

std::string returnSensorData(float pm10, float pm25, int devId)
{
  if(devId == 0){
    //Serial.println("Incomplete");
    return "Incomplete";
  }
  
  std::string output = ("PM1");
  std::string second = ("PM2");
  std::string sid = ("i");
  std::string pmTen = floatToStr(pm10).substr(0, 4);
  std::string pmTwentyFive = floatToStr(pm25).substr(0, 4);
  std::string id = intToStr(devId);
  std::string fin = output + pmTen + second + pmTwentyFive + sid + id;

  Serial.println(fin.c_str());

  return fin;
}

std::string intToStr(int num)
{
  int tempSize = 16;
  char temp[tempSize];
  sprintf(temp, "%d", num);
  char *conArr = new char[tempSize];
  for (int i = 0; i < tempSize; i++)
  {
    conArr[i] = temp[i];
  }

  //PrintCharArray(conArr, newSize);

  return std::string(conArr);
}

std::string floatToStr(float num)
{
  int tempSize = 16;
  char temp[tempSize];
  sprintf(temp, "%f", num);
  char *conArr = new char[tempSize];
  for (int i = 0; i < tempSize; i++)
  {
    conArr[i] = temp[i];
  }

  //PrintCharArray(conArr, newSize);

  return std::string(conArr);
}

int returnPosComma(float val)
{
  if (val / 10 < 1 && val / 10 > 0)
  {
    return 1;
  }

  if (val / 10 >= 1 && val / 10 < 10)
  {
    return 2;
  }

  if (val / 10 >= 1 && val / 10 < 100)
  {
    return 3;
  }

  return 0;
}

void PrintCharArray(char arr[], int sizeArr)
{
  for (int i = 0; i < sizeArr; i++)
  {
    Serial.print(arr[i]);
  }
}