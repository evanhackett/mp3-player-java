
# mp3 player

This project contains modifications to the mp3 player presented in the textbook "Objects First with Java - A Practical Introduction using BlueJ". The original code for projects in the book can be downloaded here: https://www.bluej.org/objects-first/index.html. The project is from chapter 13.

The purpose of these modifications is to demonstrate competence in the Java programming language (part of Hunter College's application criteria).

I have made the following modifications:

* added ability to change source directory of mp3 files (the original program was hardcoded to use a specific directory)
* after changing source dir, refreshes the audio files list
* changed the look/layout a bit
* changed it to just show file name instead of relying on file naming convention to parse out artist/title.
  * The original program required a specific file naming convention for the mp3 files in order to determine the artist/title/etc. Since my audio files don't use this naming convention, I got rid of this feature and instead I just display the filename itself.
* got rid of tracklist ordering dropdown (this required the file naming convention mentioned above to be useful, so I just got rid of it)
* made it so double-clicking the song file will play it (before you had to select the file in the list and then click the play button, it was very cumbersome)
* made it so pressing the enter key on a song selection will play it
* made it so change source dir dialog reopens at the current dir, instead of opening from home dir every time
* disable/enable buttons so they are only enabled when they can be reasonably used (like if no song is playing then "stop" should be disabled, for example)
* implement functionality for the slider
  * the original program's slider didn't do anything. Now it automatically moves as the song plays, and you can use it to skip around


The file where I made the most changes would definitely be `MusicPlayerGUI.java`, although I did make some changes to some of the other files as well.

## Code samples

Since the base project was provided by the authors of the aforementioned textbook, it may be difficult to tell what code I wrote and what code was provided by the textbook. One could download the original project code and compare that to the project files here. However, to make it easier, I will show some examples of code I wrote here, with some explanation.

The below examples are not an exhaustive list of all of the code I wrote, but should serve as demonstrative examples of the kind of work I did on this project.

### Example 1 - automatically adjusting slider position

The slider provided by the authors of the textbook didn't do anything. I wrote the below code to move the slider along as the track plays, so the user can see how far into the track they are. To do this I created a timer that calls a callback function on an interval. The callback function first gets the current position in the song (in "frames", not seconds/miliseconds). Then it gets the total length of the song, and then it can use the current position and the total length to calculate a percentage. The API for setting the slider takes a percentage (an int between 0 and 100), so we use that to set the sliders new value. Lastly, since we don't have a hook into the MusicPlayer class for knowing when a song finishes playing, this is a good time to check. If the song is over, we set the playback state to `STOPPED`.

```java
// Create a Timer to update the slider position periodically
timer = new Timer(100, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Update the slider position based on the current song position
        int currentPosition = player.getPosition();

        if (playbackState == PlaybackState.PLAYING && currentPosition > 0) {
            int totalLength = player.getLength();
            int currentSliderPosition = (int) Math.round(((double) currentPosition / totalLength) * 100);
            slider.setValue(currentSliderPosition);

            // also set playbackState to stopped if the song is over
            if (currentPosition == totalLength) {
                setPlaybackState(PlaybackState.STOPPED);
            }
        }
    }
});
timer.start();
```

### Example 2 - keeping track of playback state to enable/disable playback buttons

The original code from the textbook didn't keep track of the state of playback, meaning there was no way to know if a song is currently playing, paused, or stopped. This meant that the UI buttons were all enabled all the time. In order to improve the UI, I wanted to disable/enable buttons when it would make sense. To do this I had to add a mechanism for keeping track of playback state.

The first step was to define an enum for the 3 possible states.

```java
// keeps track of if an mp3 file is currently playing, paused, or stopped.
    private enum PlaybackState {
        PLAYING,
        PAUSED,
        STOPPED
    }

    private PlaybackState playbackState;
```

I then created a setPlaybackState function which also enables/disables the buttons based on the new state. This allows me to call the setPlaybackState wherever I need to, without having to think about the UI buttons every time, as the UI logic will be handled by the function.

```java
private void setPlaybackState(PlaybackState state) {
    playbackState = state;

    if (state == PlaybackState.PLAYING) {
        slider.setEnabled(true);
        playButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(true);
        pauseButton.setEnabled(true);
    } else if (state == PlaybackState.PAUSED) {
        slider.setEnabled(true);
        playButton.setEnabled(true);
        resumeButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
    } else if (state == PlaybackState.STOPPED) {
        slider.setEnabled(false);
        playButton.setEnabled(true);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }
}
```

From there I updated the play, pause, resume, and stop methods to call setPlaybackState function. For example, here is what `pause` looks like.

```java
private void pause()
{
    player.pause();
    setPlaybackState(PlaybackState.PAUSED);
}
```

### Example 3 - changing source directory

The original project didn't have a way to change the directory where the app would look for mp3 files.

First I added a new button to the toolbar of the UI.

```java
JButton button = new JButton("Set MP3 Source Directory");
button.addActionListener(e -> changeSourceDir());
toolbar.add(button);
```

Clicking on this button triggers the callback function `changSourceDir`. To implement this I had to learn about `JFileChooser`. I found the Oracle docs for Java Swing helpful: [How to Use File Choosers - docs.oracle.com](https://docs.oracle.com/javase/tutorial/uiswing///components/filechooser.html).

The code is pretty straightforward. You set the file chooser to `DIRECTORIES_ONLY` mode to make sure the user can only select a directory (and not an individual file). Then you open the chooser dialog. If the user made a selection, we set instantiate a new `MusicOrganizer` with the chosen directory. We then do some UI update stuff like refreshing the track list.

```java
/**
 * Prompts the user for a new source directory to look for music files.
 */
private void changeSourceDir()
{
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnVal = directoryChooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File dir = directoryChooser.getSelectedFile();
        organizer = new MusicOrganizer(dir);

        // refresh the track display
        setListOrdering(Track.FIELDS[0]);

        // disable play button since there won't be a selection when the new list first loads.
        playButton.setEnabled(false);
    }

}
```

I also had to update the `MusicOrganizer` class to take in a directory object upon instantiation. This was a trivial change so I won't show the code here.


## Note about bug in MusicFilePlayer, causing seek functionality to not work properly

**Note**: the seek feature for the slider seems buggy, but that may have more to do with the author's original code, rather than how I implemented the seek functionality. I am using the provided `setPosition` function within `MusicFilePlayer` to seek to a new location. The way the MusicFilePlayer is implemented seems to be causing problems when calling `setPosition`. I tested this out in a test file by instantiating a `MusicFilePlayer` object and directly calling `setPosition`, as well as `playFrom`, and indeed both methods seem to not accurately set the position. Both methods call `playFrames`, so the problem is likely there or in one of the functions that `playFrames` calls. **The GUI code I wrote involving the slider is not at fault for the buggy behavior of `MusicFilePlayer` provided by the authors of the textbook**.

## How to compile and run the program

First `cd` into the project directory.

Then compile the program:

```
javac -cp ./+libs/jl1.0.1.jar:. MusicPlayerGUI.java
```

Then run the program by specifying the classpath of the JAR dependency (the JAR contains the javazoom library for playing mp3 files):

```
java -cp ./+libs/jl1.0.1.jar:. MusicPlayerGUI
```

From there the program should be running. Select the mp3 source directory using the "Set MP3 Source Directory" button, then choose an mp3 file to play.


## Original readme

The original contents of the readme file, provided by the textbook authors, can be seen below:

---

Project: musicplayer. A project to play audio files.
Authors: David J. Barnes and Michael Kölling

This project is part of the material for the book

   Objects First with Java - A Practical Introduction using BlueJ
   Sixth edition
   David J. Barnes and Michael Kölling
   Pearson Education, 2016

It is discussed in chapter 13.

To start:

Create a MusicPlayerGUI object.
Select a track from the list and play it.

