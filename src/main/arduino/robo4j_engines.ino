#include <Wire.h>

#define SLAVE_ADDRESS 0x04
int motors;
int speed;
int direction;
int requestEngine;
boolean leftRunning;
boolean rightRunning;



void setup() {
  // put your setup code here, to run once:

  //initiate i2c as slave 
  Wire.begin(SLAVE_ADDRESS);

  //define callbacks for i2c communication
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
  Serial.begin(115200);         // start serial for output
  Serial.println("robo4j.io::arduino::tank::hello!");

  //Setup Channel A
  pinMode(12, OUTPUT); //Initiates Motor Channel A pin
  pinMode(9, OUTPUT); //Initiates Brake Channel A pin

  //Setup Channel B
  pinMode(13, OUTPUT); //Initiates Motor Channel A pin
  pinMode(8, OUTPUT);  //Initiates Brake Channel A pin
  
  //all engines stoped
  stopMotor(9);  //Engage the Brake for Channel A
  stopMotor(8);  //Engage the Brake for Channel B
  leftRunning = false;
  rightRunning = false;
  requestEngine = 0;
}

void loop() {
  // put your main code here, to run repeatedly:

}

void receiveData(int byteCount){
  byte buffer[3];

  int x=0;
  while(Wire.available()){
    buffer[x]=Wire.read();
    Serial.println("received :" + buffer[x]);
    x++;  
  }

  motors = buffer[0];
  speed = buffer[1];
  direction = buffer[2];
  
  Serial.print("array: motors:");
  Serial.print(motors);
  Serial.print(" speed:");
  Serial.print(speed);
  Serial.print(" direction:");
  Serial.println(direction);
  Serial.println("process data");


  switch(motors){
    case 1:
      requestEngine = 1;
      Serial.println("left motor");
      if(direction == 1){
        Serial.println("left motor forward");  
        runMotorForward(12,9,3, buffer[1]);
        leftRunning = true;
      } else if (direction == 2) {
        Serial.println("left motor backward");
        runMotorBackward(12,9,3, buffer[1]);  
        leftRunning = true;
      } else {
        Serial.println("left motor stop");
        stopMotor(9);  
        leftRunning = false;
      }
      break;
    case 2: 
      requestEngine = 2;
      Serial.println("right motor");
      if(direction == 1){
        Serial.println("right motor forward");  
        runMotorForward(13,8,11, buffer[1]);
        rightRunning = true;
      } else if ( direction == 2) {
        Serial.println("right motor backward");
        runMotorBackward(13,8,11, buffer[1]);  
        rightRunning = true;
      } else {
        Serial.println("right motor stop");
        stopMotor(8);  
        rightRunning = false;
      }
      break;
    default:
      Serial.println("both motors");
      switch(direction){
        case 1:
          Serial.println("both motors forward");  
          runMotorForward(12,9,3, buffer[1]);
          runMotorForward(13,8,11, buffer[1]);
          break;
        case 2:
          Serial.println("both motors backward");
          runMotorBackward(12,9,3, buffer[1]);
          runMotorBackward(13,8,11, buffer[1]); 
          break;
        default:
          Serial.println("stop both motors"); 
          stopMotor(8);
          stopMotor(9);
          break;
      }

      break;
  }
  
}

void runMotorForward(int channel, int breakCh, int speedCh, int speedVal){
  switch(channel){
    case 12:
      digitalWrite(channel, LOW);
      break;
    case 13:
      digitalWrite(channel, HIGH);
      break;
   
  }
  digitalWrite(breakCh, LOW);
  digitalWrite(speedCh, speedVal);
}

void runMotorBackward(int channel, int breakCh, int speedCh, int speedVal){
  switch(channel){
    case 12:
      digitalWrite(channel, HIGH);
      break;
    case 13:
      digitalWrite(channel, LOW);
      break;
   
  }
  digitalWrite(breakCh, LOW);
  digitalWrite(speedCh, speedVal);  
}

void stopMotor(int channel){
   digitalWrite(channel, HIGH);  
}


void sendData(){
  switch(requestEngine){
    case 1:
      if(leftRunning){
         Wire.write(22);  
      }else {
         Wire.write(11);
      }
      break;
    case 2:
      if(rightRunning){
         Wire.write(22);  
      }else {
         Wire.write(11);
      }
      break;
    default:
      Wire.write(88);
      Serial.println("requestNothing");
      break;
  }
  
}

