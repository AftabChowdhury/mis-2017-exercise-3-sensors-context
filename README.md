====================================================================
Task 3a
====================================================================
We use graphview to show the accelerometer data
x-axis=RED
y-axis=Green
z-axis=BLUE
magnitude=BLACK

to show the data please shake the device and swap the screen from right to left

For FFT press the button FFTand swap the screen from right to left

====================================================================
Task 3b
====================================================================

In application 6 music assets were used. 

Source: http://www.bensound.com/ - free-license music


We have 6 detected activity which are located in ArrayList: still, on foot, on bicycle, in vehicle, titling and unknown activity.
In addition to that, there is a Progress Bar which shows the confidence level of a particular activity. 

To reach an activity a confidence value should be at least 75.

We also tried to use the method location.getSpeed(), but, unfortunately, an unexpectable error occurred.

For the project, Google samples were used. 


How to run it properly:

1. Press Play and Pause button. The song will start. 
2. Click again Pause to pause it.
3. Clicl on Remove Updates
4. Click on Request Updates.

After last step, the application will refresh themselves every 30 seconds. You can repeat 3 and 4 step to get updates values earlier.

	