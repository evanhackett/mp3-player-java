
# mp3 player

This project contains modifications to the mp3 player presented in the textbook "Objects First with Java - A Practical Introduction using BlueJ". The original code for projects in the book can be downloaded here: https://www.bluej.org/objects-first/index.html.

The purpose of these modifications is to demonstrate competence in the Java programming language (part of Hunter College's application criteria).

I have made the following modifications:

* added ability to change source directory of mp3 files (the original program was hardcoded to use a specific directory)
* after changing source dir, refreshes the audio files list
* got rid of useless slider, got rid of background image
* changed it to just show file name instead of relying on file naming convention to parse out artist/title.
  * The original program required a specific file naming convention for the mp3 files in order to determine the artist/title/etc. Since my audio files don't use this naming convention, I got rid of this feature and instead I just display the filename itself.
* got rid of tracklist ordering dropdown (this required the file naming convention mentioned above to be useful, so I just got rid of it)
* made it so double-clicking the song file will play it (before you had to select the file in the list and then click the play button, it was very cumbersome)
* made it so pressing the enter key on a song selection will play it
* made it so change source dir dialog reopens at the current dir, instead of opening from home dir every time


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

