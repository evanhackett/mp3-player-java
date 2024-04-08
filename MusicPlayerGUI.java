import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.*;

/**
 * A simple sound player. To start, create an instance of this class.
 *
 * The sound player provides an interface to the MusicOrganizer class
 * from chapter 4.
 *
 * @author Michael Kölling and David J. Barnes
 * @version 1.0
 */
public class MusicPlayerGUI extends JFrame
{
    private static final String VERSION = "Version 1.0";
    private static final String DEFAULT_AUDIO_DIR = "./audio-files";

    private JList<String> fileList;
    private JSlider slider;
    private JLabel infoLabel;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton resumeButton;
    private MusicOrganizer organizer;
    // A player for the music tracks.
    private MusicPlayer player;
    // The current track list.
    private List<Track> trackList;
    // The directory chooser which allows users to change the mp3 source directory.
    private final JFileChooser directoryChooser;

    // keeps track of if an mp3 file is currently playing, paused, or stopped.
    private enum PlaybackState {
        PLAYING,
        PAUSED,
        STOPPED
    }

    private PlaybackState playbackState;


    /**
     * Main method for starting the player from a command line.
     */
    public static void main(String[] args)
    {
        MusicPlayerGUI gui = new MusicPlayerGUI();
    }

    /**
     * Create a SoundPlayer and display its GUI on screen.
     */
    public MusicPlayerGUI()
    {
        super("Music Player");
        File audioFolder = new File(DEFAULT_AUDIO_DIR);
        organizer = new MusicOrganizer(audioFolder);
        player = new MusicPlayer();
        directoryChooser = new JFileChooser();
        playbackState = PlaybackState.STOPPED;

        makeFrame();
    }

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

    /**
     * Play the sound file currently selected in the file list. If there is no
     * selection in the list, or if the selected file is not a sound file,
     * do nothing.
     */
    private void play()
    {
        int index = fileList.getSelectedIndex();
        if(index >= 0 && index < trackList.size()) {
            slider.setValue(0);
            player.startPlaying(trackList.get(index).getFilename());
            setPlaybackState(PlaybackState.PLAYING);
        }
    }

    /**
     * Stop the currently playing sound file (if there is one playing).
     */
    private void stop()
    {
        player.stop();
        setPlaybackState(PlaybackState.STOPPED);
    }

    /**
     * Stop the currently playing sound file (if there is one playing).
     */
    private void pause()
    {
        player.pause();
        setPlaybackState(PlaybackState.PAUSED);
    }

    /**
     * Resume a previously suspended sound file.
     */
    private void resume()
    {
        player.resume();
        setPlaybackState(PlaybackState.PLAYING);
    }

    /**
     * Skips to a section of the track.
     * @param sliderValue The position of the slider, int between 0 and 100.
     */
    private void skip(int sliderValue)
    {
        final int numFrames = player.getLength();
        // convert sliderValue into a frame position based on numFrames
        final int position = (int) Math.round((double) sliderValue / 100 * numFrames);
        player.seekTo(position);

        // seekTo causes the audio player to pause, so we want to resume it
        // if the player was in a playing state when they moved the slider
        if (playbackState == PlaybackState.PLAYING) {
            player.resume();
        }
    }

    /**
     * Display information about a selected sound file (name and clip length).
     * @param message The message to display.
     */
    private void showInfo(String message)
    {
        infoLabel.setText(message);
    }

    /**
     * Quit function: quit the application.
     */
    private void quit()
    {
        System.exit(0);
    }


    /**
     * About function: show the 'about' box.
     */
    private void showAbout()
    {
        JOptionPane.showMessageDialog(this,
                    "Music Player\n" + VERSION,
                    "About Music Player",
                    JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Set the ordering of the track list.
     * @param ordering The ordering to use.
     */
    private void setListOrdering(String ordering)
    {
        trackList = organizer.sortByField(ordering);
        String[] tracks = getTracksDisplayList(trackList);
        fileList.setListData(tracks);
    }

    /**
     * Get a display version of the track list.
     * @param trackList The list of tracks to be displayed.
     * @return The tracks in display format.
     */
    private String[] getTracksDisplayList(List<Track> trackList)
    {
        int numTracks = trackList.size();
        String[] tracks = new String[numTracks];
        for(int i = 0; i < numTracks; i++) {
            tracks[i] = trackList.get(i).getFilename();
        }
        return tracks;
    }

    // ---- Swing stuff to build the frame and all its components and menus ----

    /**
     * Create the complete application GUI.
     */
    private void makeFrame()
    {
        // the following makes sure that our application exits when
        // the user closes its window
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(6, 10, 10, 10));

        makeMenuBar();

        // Specify the layout manager with nice spacing
        contentPane.setLayout(new BorderLayout(8, 8));

        // Create the left side with combobox and scroll list
        JPanel leftPane = new JPanel();
        {
            leftPane.setLayout(new BorderLayout(8, 8));

            // Get the list of field names, used for ordering.
            String[] ordering = Track.FIELDS;

            // Create the combo box.
            JComboBox<String> formatList = new JComboBox<>(ordering);
            formatList.addActionListener(e -> {
                int index = formatList.getSelectedIndex();
                if(index >= 0) {
                    String selectedOrder = formatList.getItemAt(index);
                    setListOrdering(selectedOrder);
                }
            });

            // Create the scrolled list for track listing.
            fileList = new JList<>();
            fileList.setForeground(new Color(140,171,226));
            fileList.setBackground(new Color(0,0,0));
            fileList.setSelectionBackground(new Color(87,49,134));
            fileList.setSelectionForeground(new Color(140,171,226));

            fileList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedIndex = fileList.getSelectedIndex();
                        if (selectedIndex != -1) {
                            playButton.setEnabled(true);
                        }
                    }
                }
            });

            // play on double-click
            fileList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    if (me.getClickCount() == 2) {
                        play();
                    }
                }
            });

            // play on "enter" key press
            fileList.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent ke) {
                    int key = ke.getKeyCode();
                    if(key == KeyEvent.VK_ENTER) {
                        play();
                    }
                 }
            });

            JScrollPane scrollPane = new JScrollPane(fileList);
            scrollPane.setColumnHeaderView(new JLabel("Audio files"));
            leftPane.add(scrollPane, BorderLayout.CENTER);

            // Set up the initial listing.
            setListOrdering(ordering[0]);
        }
        contentPane.add(leftPane, BorderLayout.CENTER);

        // Create the center with text label, and slider
       JPanel centerPane = new JPanel();
        {
            centerPane.setLayout(new BorderLayout(8, 8));

            centerPane.setBackground(Color.BLACK);

            infoLabel = new JLabel("  ");
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoLabel.setForeground(new Color(140,171,226));
            centerPane.add(infoLabel, BorderLayout.CENTER);

            slider = new JSlider(0, 100, 0);
            TitledBorder border = new TitledBorder("Seek");
            border.setTitleColor(Color.white);
            slider.setBorder(new CompoundBorder(new EmptyBorder(6, 10, 10, 10), border));
            slider.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (slider.isEnabled()) {
                        int sliderValue = slider.getValue();
                        skip(sliderValue);
                    }
                }
            });
            slider.setBackground(Color.BLACK);
            slider.setMajorTickSpacing(25);
            slider.setPaintTicks(true);
            slider.setEnabled(false);
            centerPane.add(slider, BorderLayout.SOUTH);
        }
        contentPane.add(centerPane, BorderLayout.EAST);

        // Create the toolbar with the buttons
        JPanel toolbar = new JPanel();
        {
            toolbar.setLayout(new GridLayout(1, 0));

            JButton button = new JButton("Set MP3 Source Directory");
            button.addActionListener(e -> changeSourceDir());
            toolbar.add(button);

            playButton = new JButton("Play");
            playButton.addActionListener(e -> play());
            toolbar.add(playButton);
            playButton.setEnabled(false);

            stopButton = new JButton("Stop");
            stopButton.addActionListener(e -> stop());
            toolbar.add(stopButton);
            stopButton.setEnabled(false);

            pauseButton = new JButton("Pause");
            pauseButton.addActionListener(e -> pause());
            toolbar.add(pauseButton);
            pauseButton.setEnabled(false);

            resumeButton = new JButton("Resume");
            resumeButton.addActionListener(e -> resume());
            toolbar.add(resumeButton);
            resumeButton.setEnabled(false);
        }

        contentPane.add(toolbar, BorderLayout.NORTH);

        // building is done - arrange the components
        pack();

        // place this frame at the center of the screen and show
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d.width/2 - getWidth()/2, d.height/2 - getHeight()/2);
        setVisible(true);
    }

    /**
     * Create the main frame's menu bar.
     */
    private void makeMenuBar()
    {
        final int SHORTCUT_MASK =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        JMenu menu;
        JMenuItem item;

        // create the File menu
        menu = new JMenu("File");
        menubar.add(menu);

        item = new JMenuItem("Quit");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_MASK));
            item.addActionListener(e -> quit());
        menu.add(item);

        // create the Help menu
        menu = new JMenu("Help");
        menubar.add(menu);

        item = new JMenuItem("About Music Player...");
            item.addActionListener(e -> showAbout());
        menu.add(item);
    }
}
