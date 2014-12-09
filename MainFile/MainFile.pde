import android.content.Intent;


String mainColor;
String MapPageTitle;
String AppName;

int radiusInMiles;


//Set your options up!
void setup(){
  
  //Color of the topBar
  mainColor = "#bcdbcd";
  
  
  AppName = "Awesome";
  MapPageTitle = "Map Page";
  //Set Radius for Map
  radiusInMiles = 45;
  
  
  
  
  runApplication();
}


//Now boot up our application & have at it!
void runApplication(){
  try{
    Intent intent = getPackageManager().getLaunchIntentForPackage("edu.fau.communityupgrade");
    intent.putExtra("MAIN_COLOR", mainColor);
    intent.putExtra("RADIUS_IN_MILES", radiusInMiles);
    intent.putExtra("MAP_PAGE_TITLE",MapPageTitle);
    intent.putExtra("APPLICATION_NAME",AppName);
    startActivity(intent);
  }
  catch(Exception ex){
    System.out.println("test");
  }
}
