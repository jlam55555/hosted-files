/**
  * AP CSP Processing Final Project
  * ---
  * Control Joe the ninja fruit-thief as he makes a prime snatch! Hide when it's light and steal when it's dark!
  * <p>
  * Objective: steal all the food in the fastest time without losing all your lives. You lose a life if you get caught in the light.
  * <p>
  * Controls:
  * - Left arrow key:   move left
  * - Right arrow key:  move right
  * - Up arrow key:     steal food
  * - Down arrow key:   hide
  * <p>
  * Enjoy!
  *
  * @author Jonathan Lam
  */

// customizable game constants
// all values are in pixels, pixels per frame, or frames
final float headSize = 120;
final float personVelocity = 10;
final float leftBound = -1e4;
final float rightBound = 1e4;
final int numFoods = 50;
final float foodSize = 30;
final float threshold = 40;
final float lineWidth = 5;
final int numLives = 3;
final int framesLightsOn = 60;      // 60 frames = 2 seconds lights on
final int framesWarning = 20;       // 20 frames = 1/3 second warning light
final int meanLightFrequency = 300; // 300 frames = 5 seconds before next light off on average

// keycode constants for convenience
final int LEFT = 37;
final int RIGHT = 39;
final int UP = 38;
final int DOWN = 40;

// game variables
float personXPos = 0;
int lives = numLives;

// animatable interface for consistency
abstract class Animatable {
  int status = 0;
  abstract void draw(float xPos, float yPos);
}

// class for animated objects
class Animation {
  Animatable object;
  float xPos;
  float startXPos;
  float endXPos;
  float yPos;
  float startYPos;
  float endYPos;
  float numFrames;
  int frame = 0;
  int index = 0;
  Animation(Animatable object, float xPos, float yPos, float endXPos, float endYPos, float numFrames) {
    this.object = object;
    this.xPos = xPos;
    this.startYPos = xPos;
    this.endXPos = endXPos;
    this.yPos = yPos;
    this.startYPos = yPos;
    this.endYPos = endYPos;
    this.numFrames = numFrames;
    animates.add(this);
  }
  void next() {
    if(this.frame++ == this.numFrames) {
      this.object.status = 2;
      for(int i = 0; i < animates.size(); i++) {
        if(animates.get(i) == this) {
          animates.remove(i);
          return; 
        }
      }
    }
    this.yPos += (this.endYPos - this.startYPos) / numFrames;
    this.xPos += (this.endXPos - this.startXPos) / numFrames;
    this.object.draw(this.xPos, this.yPos);
  }
}
ArrayList<Animation> animates = new ArrayList<Animation>();

// food class
class Food extends Animatable {
  float xPos;
  String type;
  boolean eaten = false;
  int status = 0;
  Food(float xPos, String type) {
    this.xPos = xPos;
    this.type = type;
  }
  float getX() {
    return this.status == 1 ? width/2 : width/2 + (this.xPos - personXPos);
  }
  float getY(float shift) {
    return height/2 + 2*foodSize + shift;
  }
  void draw(float xPos, float yPos) {
    if(this.status == 2) return;
    switch(this.type) {
      case "orange":
        fill(0xffff9900);
        ellipse(this.getX(), this.getY(yPos), foodSize, foodSize);
        break;
      case "apple":
        fill(0xffff0000);
        ellipse(this.getX(), this.getY(yPos), foodSize, foodSize);
        fill(0xff663300);
        line(this.getX(), this.getY(yPos)-foodSize/2, this.getX(), this.getY(yPos)-foodSize);
        fill(0xff00aa00);
        triangle(this.getX(), this.getY(yPos)-foodSize/2, this.getX()+foodSize/2, this.getY(yPos)-foodSize, this.getX()+foodSize*2/3, this.getY(yPos)-foodSize/2);
    }
  }
  void eat() {
    new Animation(this, 0, 0, 0, -headSize*2/3-2*foodSize, 10);
    this.status = 1;
  }
}
// array of foods
ArrayList<Food> foods = new ArrayList<Food>();
String[] foodTypes = { "orange", "apple" };

// setup
void setup() {
  // set up background and line width
  size(1000, 600);
  strokeWeight(lineWidth);
  
  // create foods
  for(int i = 0; i < numFoods; i++) {
    String foodType = foodTypes[(int) Math.floor(Math.random() * foodTypes.length)];
    float foodPos = (float) (Math.random() * (rightBound - leftBound)) + leftBound;
    foods.add(new Food(foodPos, foodType));
  }
}

// endgame
void showText(String text, int col) {
  textAlign(CENTER);
  textSize(50);
  fill(col);
  text(text, width/2, height/2);
}
void endGame(boolean win) {
  int col = win ? 0xffffffff : 0xffff0000;
  showText(win ? "YOU WIN. TIME: " + (millis() / 1000) + " SECONDS" : "YOU LOSE", col);
  noLoop();
}

int framesSinceLightsOn = -1;
boolean caughtThisTime = false;
void draw() {
  // dark background
  background(framesSinceLightsOn >= framesWarning ? 0xffffffff : framesSinceLightsOn >= 0 ? 0xff888888 : 0xff666666);
  
  // control lights on/off
  // if on turn off when necessary
  if(framesSinceLightsOn >= 0) {
    if(framesSinceLightsOn >= framesWarning && !keyMap[DOWN] && !caughtThisTime) {
      lives--;
      caughtThisTime = true;
    }
    if(framesSinceLightsOn++ >= framesLightsOn+framesWarning) {
      framesSinceLightsOn = -1;
      caughtThisTime = false;
    }
  }
  // if off turn on by random
  else if(Math.random() * (meanLightFrequency-framesWarning) <= 1) {
    framesSinceLightsOn = 0;
  }
  
  // show lives down if caught
  if(caughtThisTime && lives != 0)
    showText("LIVES REMAINING: " + lives, 0xffff0000);

  // write lives and score in top left
  fill(0xff000000);
  int numEaten = 0;
  for(int i = 0; i < foods.size(); i++) {
    if(foods.get(i).status >= 1) numEaten++;
  }
  int remaining = numFoods - numEaten;
  int time = (int) Math.floor(millis() / 1000);
  textAlign(LEFT);
  textSize(12);
  text("Foods remaining: " + remaining, 10, 20);
  text("Time: " + time, 10, 40);
  text("Lives: " + lives, 10, 60);
  
  // if win or lose
  if(remaining == 0) {
    endGame(true);
    return;
  } else if(lives == 0) {
    endGame(false);
    return;
  }

  // draw person
  noFill();
  
  // if ducking hide
  float shift = 0;
  if(keyMap[DOWN]) {
    stroke(0x44000000);
    shift = headSize/6;
  }
  // draw the head
  ellipse(width/2, height/2-headSize+shift, headSize, headSize);
  line(width/2, height/2-headSize/2+shift, width/2, height/2+shift);
  
  // get adjustment based on movement
  int tilt = keyMap[LEFT] ? -5 : keyMap[RIGHT] ? 5 : 0;
  
  // draw the mask
  pushStyle();
  noStroke();
  fill(keyMap[DOWN] ? 0x44000000 : 0xff000000);
  quad(width/2-headSize/2, height/2-headSize-headSize/8+shift,
       width/2-headSize/2, height/2-headSize+headSize/8+shift,
       width/2+headSize/2, height/2-headSize+headSize/8+shift,
       width/2+headSize/2, height/2-headSize-headSize/8+shift);
  popStyle();
  
  // draw the mouth
  if(animates.size() > 0) {
    ellipse(width/2+tilt, height/2-headSize*2/3+shift, foodSize, foodSize);
  } else {
    line(width/2-foodSize/2+tilt, height/2-headSize*2/3+shift, width/2+foodSize/2+tilt, height/2-headSize*2/3+shift);
  }
  
  // draw arms, depending on orientation
  line(width/2-headSize/3, height/2-headSize/3+tilt+shift, width/2+headSize/3, height/2-headSize/3-tilt+shift);
  
  // fix adjusted values if necessary
  stroke(0xff000000);
  
  // draw counter
  fill(0xff663300);
  rect((width/2+leftBound)-personXPos-threshold, height/2, rightBound-leftBound+threshold*2, height+lineWidth);
  
  // show instructions
  textAlign(CENTER);
  textSize(20);
  fill(0xffffffff);
  text("Eat all the food without getting caught!", width/2, height-50);
  
  // draw fruit
  for(int i = 0; i < foods.size(); i++) {
    if(foods.get(i).status == 0)
      foods.get(i).draw(0, 0);
  }
  
  // update person position if not hiding
  if(!keyMap[DOWN]) {
    if(keyMap[LEFT]) {
       personXPos -= personVelocity;
    }
    if(keyMap[RIGHT]) {
      personXPos += personVelocity;
    }
  }
  
  // bound person's movement
  personXPos = Math.min(rightBound, Math.max(leftBound, personXPos));
  
  // draw all animations
  for(int i = 0; i < animates.size(); i++) {
    animates.get(i).next();
  }
}

// keymap keeps track of which keys are pressed
boolean[] keyMap = new boolean[41];
void keyPressed() {
  switch(keyCode) {
    // left and right keys for movement
    case LEFT:
      keyMap[LEFT] = true;
      keyMap[RIGHT] = false;
      break;
    case RIGHT:
      keyMap[RIGHT] = true;
      keyMap[LEFT] = false;
      break;
    // up key to eat food
    case UP:
      // check if any nearby food
      for(int i = 0; i < foods.size(); i++) {
        if(Math.abs(foods.get(i).xPos - personXPos) < threshold && foods.get(i).status == 0) {
          foods.get(i).eat();
          return;
        }
      }
      // if food is nearby, attempt to eat
      break;
    // down key to duck
    case DOWN:
      keyMap[DOWN] = true;
      keyMap[UP] = false;
      break;
  }
}
void keyReleased() {
  switch(keyCode) {
    case LEFT:
      keyMap[LEFT] = false;
      break;
    case RIGHT:
      keyMap[RIGHT] = false;
      break;
    case UP:
      keyMap[UP] = false;
      break;
    case DOWN:
      keyMap[DOWN] = false;
      break;
  }
}