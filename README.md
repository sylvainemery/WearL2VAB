Open the **"Les 2 Vaches au bureau"** fridge from your Android Wear device.

The app has two parts:
- the mobile app is where the connection to the fridge happens
- the wear app is the interactive part

How to install
--------------

- first, connect the wear device to your mobile device
- follow instructions here https://developer.android.com/training/wearables/apps/bt-debugging.html to enable debugging the wear app over bluetooth
- Open the project in Android Studio
- copy the `/mobile/src/main/res/values/auth.xml.example` to `auth.xml`, and fill in the info
- run the mobile project to install it on the connected mobile device
- run the wear project to install it on the connected wear device
