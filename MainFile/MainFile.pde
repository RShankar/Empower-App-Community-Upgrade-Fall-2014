import android.content.Intent;


String mainColor;
String MapPageTitle;
String ListPageTitle;
String AppName;

int radiusInMiles;

int titleTextSize;
int defaultTextSize;

String upvoteButtonColor;
String downvoteButtonColor;

//Set your options up!
void setup() {

  //Color of the topBar
  mainColor = "#4EC9F2";
  AppName = "Awesome";
  MapPageTitle = "Map Page";
  ListPageTitle = "Places Near You";

  //Set Radius for Map
  radiusInMiles = 45;

  //Set TExt Sizes for App
  titleTextSize = 22;
  defaultTextSize = 15;

  upvoteButtonColor = "#F24E4E";
  downvoteButtonColor = "#F24ECC";

  runApplication();
}


//Now boot up our application & have at it!
void runApplication() {
  try {
    Intent intent = getPackageManager().getLaunchIntentForPackage("edu.fau.communityupgrade");
    intent.putExtra("MAIN_COLOR", mainColor);
    intent.putExtra("RADIUS_IN_MILES", radiusInMiles);
    intent.putExtra("MAP_PAGE_TITLE", MapPageTitle);
    intent.putExtra("LIST_PAGE_TITLE", ListPageTitle);
    intent.putExtra("DEFAULT_TEXT_SIZE", defaultTextSize);
    intent.putExtra("TITLE_TEXT_SIZE", titleTextSize);
    intent.putExtra("APPLICATION_NAME", AppName);
    startActivity(intent);
  }
  catch(Exception ex) {
    System.out.println("test");
  }
}

