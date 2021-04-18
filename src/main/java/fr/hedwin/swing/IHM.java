/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 Copyright (c) 2021.
 Project: Projet 2A
 Author: Edwin HELET & Julien GUY
 Class: IHM.java
 :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

package fr.hedwin.swing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import fr.hedwin.Main;
import fr.hedwin.db.TMDB;
import fr.hedwin.db.model.TmdbElement;
import fr.hedwin.db.object.DbMovie;
import fr.hedwin.db.object.DbSerie;
import fr.hedwin.db.utils.CompletableFuture;
import fr.hedwin.db.utils.Future;
import fr.hedwin.objects.Movie;
import fr.hedwin.swing.panel.result.MultipleResultPanel;
import fr.hedwin.swing.panel.utils.form.Form;
import fr.hedwin.swing.panel.utils.form.FormActionEntry;
import fr.hedwin.swing.panel.utils.form.FormSingleEntry;
import fr.hedwin.swing.window.FormDialog;
import fr.hedwin.swing.window.ResultsDialog;
import fr.hedwin.swing.other.LoadDataBar;
import fr.hedwin.swing.panel.*;
import fr.hedwin.swing.panel.result.properties.ResultPanelProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IHM extends JFrame {

    public static IHM INSTANCE;
    private final LoadDataBar progressData = new LoadDataBar();
    private JTabbedPane onglets;
    private int notifCinematheque = 0;
    private Cinematheque cinematheque;
    private SearchPanel searchPanel;

    public IHM() {
        super("Cinémathèque");
        INSTANCE = this;
        initComponents();
    }

    private void initComponents() {
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));
        setIconImage(icon);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeIHM();
            }
        });
        setJMenuBar(new MenuBar(this));
        setPreferredSize(new Dimension(1280, 720));
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


        /*
         * BAR DE CONNEXION
         */

        JPanel cib = new JPanel();
        cib.setLayout(new BoxLayout(cib, BoxLayout.X_AXIS));
        cib.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(cib);

        JButton settings = new JButton();
        settings.setIcon(new FlatSVGIcon("images/settings.svg"));
        settings.setFocusable(false);
        settings.setBorderPainted(false);

        JLabel pp = new JLabel();
        //pp.setIcon(new ImageIcon(new RoundImage(getClass().getResource("/images/user_dark.svg")).generate().getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
        pp.setIcon(new ImageIcon(new FlatSVGIcon("images/user_dark.svg").derive(16, 16).getImage()));
        cib.add(Box.createHorizontalGlue());
        cib.add(pp);
        cib.add(Box.createRigidArea(new Dimension(10, 0)));
        cib.add(new JLabel("Edwin"));
        cib.add(Box.createRigidArea(new Dimension(10, 0)));
        cib.add(settings);

        /*
         * ONGLETS
         */
        Cinematheque cinematheque = new Cinematheque(this);
        this.cinematheque = cinematheque;

        SearchPanel searchPanel = new SearchPanel(this);
        this.searchPanel = searchPanel;

        JTabbedPane onglets = new JTabbedPane();
        this.onglets = onglets;
        onglets.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        onglets.addTab("Cinémathèque", new FlatSVGIcon("images/addToDictionary_dark.svg"), cinematheque);
        onglets.addTab("Rechercher", new FlatSVGIcon("images/web_dark.svg"), searchPanel);
        onglets.addChangeListener(evt -> {
            if (onglets.getSelectedIndex() == 0) {
                onglets.setIconAt(0, new FlatSVGIcon("images/addToDictionary_dark.svg"));
                notifCinematheque = 0;
            }
        });

        onglets.add("A propos", new JLabel());
        add(onglets);

        //add(new JSeparator(SwingConstants.HORIZONTAL));

        /*
         * BAR D'OUTIL EN BAS
         */
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        JButton addMovie = new JButton("Ajouter un film ou une serie");
        addMovie.setMaximumSize(addMovie.getPreferredSize());
        addMovie.addActionListener(evt -> openFormAdd());

        JButton button = new JButton();
        button.setIcon(new FlatSVGIcon("images/download.svg"));
        button.addActionListener(evt -> {
            JFileChooser jFileChooser = new JFileChooser();
            int retour = jFileChooser.showOpenDialog(this);
            if (retour == JFileChooser.APPROVE_OPTION) {
                System.out.println(jFileChooser.getSelectedFile().getName());
                System.out.println(jFileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        button.setFocusable(false);
        button.setBorderPainted(false);

        btns.add(addMovie);
        btns.add(Box.createHorizontalGlue());
        btns.add(progressData);
        btns.add(Box.createRigidArea(new Dimension(20, 0)));
        btns.add(button);
        add(btns);

        setResizable(true);
        pack();
        setLocationRelativeTo(null);
    }

    public LoadDataBar getProgressData() {
        return progressData;
    }

    public Cinematheque getCinematheque() {
        return cinematheque;
    }

    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    public void selectedTab(int i){
        onglets.setSelectedIndex(i);
    }

    public void addNotifTab(int tab) {
        if (this.onglets.getSelectedIndex() == tab) return;
        notifCinematheque++;
        String fileName = (notifCinematheque > 99 ? 100 : notifCinematheque) + "";
        this.onglets.setIconAt(tab, new FlatSVGIcon("images/notif/" + fileName + ".svg"));
    }

    public void addNotifCinematheque() {
        addNotifTab(0);
    }

    public void closeIHM() {
        Object[] choices = new Object[]{"Oui", "Non", "Annuler"};
        int i = JOptionPane.showOptionDialog(getContentPane(), "Sauvegarder les données ?", "Fermeture de l'application", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, choices, choices[0]
        );
        if (i == 0) {
            try {
                Main.saveDatas();
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(getContentPane(), ioException.getMessage(), "Erreur lors de la sauvegarde", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (i != 2) {
            dispose();
            System.exit(0);
        }
    }

    public void openFormAdd() {
        FormDialog formDialog = new FormDialog(this, "Ajouter un film ou une série", true);
        FormSingleEntry<String> formEntrie = new FormSingleEntry<>("NOM", null, s->s, s->s);
        FormSingleEntry<Movie.Format> btn_group = new FormSingleEntry<>("FORMAT", null, Movie.Format::toString, Movie.Format::getIndice, r -> true, FormSingleEntry.Type.RADIOBUTTON, Movie.Format.values());
        FormActionEntry add = new FormActionEntry("Ajouter", () -> {
            try {
                Map<String, Future<?>> futureMap = new HashMap<String, Future<?>>(){{{
                    put("Film", TMDB.searchMovie(formEntrie.getValue()));
                    put("Serie", TMDB.searchTvSerie(formEntrie.getValue()));
                }}};
                getProgressData().initialize();

                ResultPanelProperties<TmdbElement> resultPanelProperties = new ResultPanelProperties<>("Choisir ce film", o -> {
                    try {
                        createMovie(o, btn_group);
                        formDialog.dispose();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.WARNING_MESSAGE);
                    }
                }, "Ne rien choisir", () -> {
                    try {
                        createMovie(formEntrie, btn_group);
                        formDialog.dispose();
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(this, e2.getMessage(), "Erreur", JOptionPane.WARNING_MESSAGE);
                    }
                });

                CompletableFuture.async(() -> futureMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    try {
                        return e.getValue().call();
                    } catch (Exception exception) {
                        return null;
                    }
                }))).then((map) -> {
                    try {
                        ResultsDialog ihmSearch = new ResultsDialog(formDialog, true, new MultipleResultPanel(map, progressData, resultPanelProperties));
                        ihmSearch.setVisible(true);
                    } catch (Exception e) {
                        try {
                            createMovie(formEntrie, btn_group);
                            formDialog.dispose();
                        } catch (Exception exception) {
                            JOptionPane.showMessageDialog(this, exception.getMessage(), "Erreur", JOptionPane.WARNING_MESSAGE);
                        }
                    } finally {
                        getProgressData().close();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, e -> {});
        Form form = new Form("Ajouter", formEntrie, btn_group, add);
        form.setPreferredSize(new Dimension(300, 300));
        formDialog.initComponents(form);
    }

    public void createMovie(TmdbElement tmdbElement, FormSingleEntry<Movie.Format> btn_group) throws Exception {
        Movie movie = null;
        if(tmdbElement instanceof DbMovie){
            DbMovie dbMovie = (DbMovie) tmdbElement;
            movie = new Movie(dbMovie.getId(), dbMovie.getTitle(), btn_group.getValue(), new Date());
        }else if(tmdbElement instanceof DbSerie){
            DbSerie dbSerie = (DbSerie) tmdbElement;
            movie = new Movie(dbSerie.getId(), dbSerie.getName(), btn_group.getValue(), new Date());
        }
        if(movie != null) {
            IHM.INSTANCE.addMovie(UUID.randomUUID(), movie);
            IHM.INSTANCE.addNotifCinematheque();
        }
    }

    public void createMovie(FormSingleEntry<String> formEntrie, FormSingleEntry<Movie.Format> btn_group) throws Exception {
        Movie movie = new Movie(formEntrie.getValue(), btn_group.getValue(), new Date());
        IHM.INSTANCE.addMovie(UUID.randomUUID(), movie);
        IHM.INSTANCE.addNotifCinematheque();
    }

    public void addMovie(UUID uuid, Movie movie) {
        Main.movies.put(uuid, movie);
        getCinematheque().getTable().addRow(uuid, movie);
    }

    public static boolean isAlreadyAdded(int id) {
        return Main.movies.values().stream().map(Movie::getIdTMDBLink).anyMatch(i -> i == id);
    }

    public static UUID getUUIDFromMovie(Movie movie){
        return Main.movies.entrySet().stream().filter(e -> e.getValue().equals(movie)).findFirst().map(Map.Entry::getKey).orElse(null);
    }

}