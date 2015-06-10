Open the **"Les 2 Vaches au bureau"** fridge from the notification on your Android Wear device.

The mobile app listens to a specific iBeacon, and sends a notification to the Wear device when the mobile is near the iBeacon.
From the notification, you can open the fridge.

How to install
--------------

- first, connect the wear device to your mobile device
- Open the project in Android Studio
- copy the `/mobile/src/main/res/values/auth.xml.example` to `auth.xml`, and fill in the info
- change the iBeacon identifiers in the `/mobile/src/main/res/values/beacon.xml`
- run the mobile project to install it on the connected mobile device
