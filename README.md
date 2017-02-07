# android-devtools
- You can change screen and density resolution (good to take some more juice on games and test how a different screen will handle your app);
- You can connect your device adb via wifi using ADB TCP port 5555 (therefore you can work without usb cable);

For security and safety reasons some firmwares require root permission. In order to it work you should have a rooted device or a firmware that let you change this settings OR you have to connect adb your device in a regular way (usb) and call adb 'adb shell pm grant com.webdefault.devtools android.permission.WRITE_SECURE_SETTINGS'.

The source code is hosted on github at https://github.com/orlleite/android-devtools. You're welcome to make improves.
