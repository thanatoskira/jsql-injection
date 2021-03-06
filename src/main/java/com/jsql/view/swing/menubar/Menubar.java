/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.menubar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.util.GitUtil;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.action.ActionHandler;
import com.jsql.view.swing.action.ActionNewWindow;
import com.jsql.view.swing.action.ActionSaveTab;
import com.jsql.view.swing.dialog.DialogAbout;
import com.jsql.view.swing.dialog.DialogPreferences;
import com.jsql.view.swing.dialog.DialogTranslate;
import com.jsql.view.swing.dialog.Language;
import com.jsql.view.swing.scrollpane.LightScrollPane;
import com.jsql.view.swing.table.PanelTable;
import com.jsql.view.swing.text.JPopupTextArea;
import com.jsql.view.swing.text.JTextFieldPlaceholder;

/**
 * Application main menubar.
 */
@SuppressWarnings("serial")
public class Menubar extends JMenuBar {
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(Menubar.class);

    /**
     * Checkbox item to show/hide chunk console.
     */
    public JCheckBoxMenuItem chunkMenu;

    /**
     * Checkbox item to show/hide binary console.
     */
    public JCheckBoxMenuItem binaryMenu;

    /**
     * Checkbox item to show/hide network panel.
     */
    public JCheckBoxMenuItem networkMenu;

    /**
     * Checkbox item to show/hide java console.
     */
    public JCheckBoxMenuItem javaDebugMenu;

    /**
     * Create a menubar on main frame.
     */
    public Menubar() {
        // File Menu > save tab | exit
        JMenu menuFile = new JMenu(I18n.valueByKey("MENUBAR_FILE"));
        I18n.addComponentForKey("MENUBAR_FILE", menuFile);
        menuFile.setMnemonic('F');

        JMenuItem itemNewWindows = new JMenuItem(new ActionNewWindow());

        JMenuItem itemSave = new JMenuItem(new ActionSaveTab());

        JMenuItem itemExit = new JMenuItem(I18n.valueByKey("MENUBAR_FILE_EXIT"), 'x');
        I18n.addComponentForKey("MENUBAR_FILE_EXIT", itemExit);
        itemExit.setIcon(HelperUi.EMPTY);
        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                MediatorGui.frame().dispose();
            }
        });

        ActionHandler.addShortcut(Menubar.this);

        menuFile.add(itemNewWindows);
        menuFile.add(new JSeparator());
        menuFile.add(itemSave);
        menuFile.add(new JSeparator());
        menuFile.add(itemExit);

        // Edit Menu > copy | select all
        JMenu menuEdit = new JMenu(I18n.valueByKey("MENUBAR_EDIT"));
        I18n.addComponentForKey("MENUBAR_EDIT", menuEdit);
        menuEdit.setMnemonic('E');

        JMenuItem itemCopy = new JMenuItem(I18n.valueByKey("CONTEXT_MENU_COPY"), 'C');
        I18n.addComponentForKey("CONTEXT_MENU_COPY", itemCopy);
        itemCopy.setIcon(HelperUi.EMPTY);
        itemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        itemCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MediatorGui.tabResults().getSelectedComponent() instanceof PanelTable) {
                    ((PanelTable) MediatorGui.tabResults().getSelectedComponent()).copyTable();
                } else if (MediatorGui.tabResults().getSelectedComponent() instanceof JScrollPane) {
                    ((JTextArea) ((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView()).copy();
                }
            }
        });

        JMenuItem itemSelectAll = new JMenuItem(I18n.valueByKey("CONTEXT_MENU_SELECT_ALL"), 'A');
        I18n.addComponentForKey("CONTEXT_MENU_SELECT_ALL", itemSelectAll);
        itemSelectAll.setIcon(HelperUi.EMPTY);
        itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        itemSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MediatorGui.tabResults().getSelectedComponent() instanceof PanelTable) {
                    ((PanelTable) MediatorGui.tabResults().getSelectedComponent()).selectTable();
                // Textarea need focus to select all
                } else if (MediatorGui.tabResults().getSelectedComponent() instanceof JScrollPane) {
                    ((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView().requestFocusInWindow();
                    ((JTextArea) ((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView()).selectAll();
                }
            }
        });

        menuEdit.add(itemCopy);
        menuEdit.add(new JSeparator());
        menuEdit.add(itemSelectAll);

        // Window Menu > Preferences
        JMenu menuWindows = new JMenu(I18n.valueByKey("MENUBAR_WINDOWS"));
        I18n.addComponentForKey("MENUBAR_WINDOWS", menuWindows);
        menuWindows.setMnemonic('W');

        JMenu menuTranslation = new JMenu("Language");
        JMenuItem itemEnglish = new JRadioButtonMenuItem(
            "<html><span style=\"font-family:'Monospace'\">"+ new Locale("en").getDisplayLanguage(new Locale("en")) +"</span></html>",
            HelperUi.FLAG_EN, 
            !ArrayUtils.contains(new Locale[]{Locale.FRENCH}, Locale.getDefault())
        );
        itemEnglish.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                I18n.setLocaleDefault(ResourceBundle.getBundle("com.jsql.i18n.jsql", Locale.ROOT));
                Menubar.this.switchLocale();                
            }
        });
        menuTranslation.add(itemEnglish);
        JMenuItem itemFrench = new JRadioButtonMenuItem(
            "<html><span style=\"font-family:'Monospace'\">"+ new Locale("fr").getDisplayLanguage(new Locale("fr")) +"</span></html>",
            HelperUi.FLAG_FR, 
            ArrayUtils.contains(new Locale[]{Locale.FRENCH}, Locale.getDefault())
        );
        itemFrench.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                I18n.setLocaleDefault(ResourceBundle.getBundle("com.jsql.i18n.jsql", Locale.FRENCH));
                Menubar.this.switchLocale();                
            }
        });
        menuTranslation.add(itemFrench);
        
        ButtonGroup groupRadioLanguage = new ButtonGroup();
        groupRadioLanguage.add(itemEnglish);
        groupRadioLanguage.add(itemFrench);
        
        JMenu menuI18nContribution = new JMenu("I help translate jSQL into");
        
        // Render the About dialog behind scene
        final DialogTranslate dialogTranslate = new DialogTranslate();
        
        class ActionTranslate implements ActionListener {
            Language language;
            
            ActionTranslate(Language language) {
                this.language = language;
            }
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dialogTranslate.reinit(this.language);
                
                // Center the dialog
                if (!dialogTranslate.isVisible()) {
                    dialogTranslate.setSize(640, 460);
                    dialogTranslate.setLocationRelativeTo(MediatorGui.frame());
                    dialogTranslate.getRootPane().setDefaultButton(dialogTranslate.buttonSend);
                }
                
                dialogTranslate.setVisible(true);
            }
        }
        
        JMenuItem itemIntoHindi = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("hi").getDisplayLanguage(new Locale("hi")) +"</span>...</html>", HelperUi.FLAG_IN);
        JMenuItem itemIntoArabic = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("ar").getDisplayLanguage(new Locale("ar")) +"</span>...</html>", HelperUi.FLAG_AR);
        JMenuItem itemIntoRussia = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("ru").getDisplayLanguage(new Locale("ru")) +"</span>...</html>", HelperUi.FLAG_RU);
        JMenuItem itemIntoChina = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("zh").getDisplayLanguage(new Locale("zh")) +"</span>...</html>", HelperUi.FLAG_CN);
        JMenuItem itemIntoFrench = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("fr").getDisplayLanguage(new Locale("fr")) +"</span>...</html>", HelperUi.FLAG_FR);
        JMenuItem itemIntoTurkey = new JMenuItem("<html><span style=\"font-family:'Monospace'\">"+ new Locale("tr").getDisplayLanguage(new Locale("tr")) +"</span>...</html>", HelperUi.FLAG_TR);
        JMenuItem itemIntoOther = new JMenuItem("another language...");
        
        itemIntoArabic.setHorizontalAlignment(SwingConstants.TRAILING);
        itemIntoArabic.setHorizontalTextPosition(SwingConstants.LEADING);
        
        menuI18nContribution.add(itemIntoHindi);
        menuI18nContribution.add(itemIntoArabic);
        menuI18nContribution.add(itemIntoRussia);
        menuI18nContribution.add(itemIntoChina);
        menuI18nContribution.add(itemIntoFrench);
        menuI18nContribution.add(itemIntoTurkey);
        menuI18nContribution.add(new JSeparator());
        menuI18nContribution.add(itemIntoOther);
        
        itemIntoHindi.addActionListener(new ActionTranslate(Language.IN));
        itemIntoArabic.addActionListener(new ActionTranslate(Language.AR));
        itemIntoRussia.addActionListener(new ActionTranslate(Language.RU));
        itemIntoChina.addActionListener(new ActionTranslate(Language.CN));
        itemIntoTurkey.addActionListener(new ActionTranslate(Language.TR));
        itemIntoFrench.addActionListener(new ActionTranslate(Language.FR));
        itemIntoOther.addActionListener(new ActionTranslate(Language.OT));
        
        menuWindows.add(menuTranslation);
        menuWindows.add(new JSeparator());
        
        JMenu menuView = new JMenu(I18n.valueByKey("MENUBAR_VIEW"));
        I18n.addComponentForKey("MENUBAR_VIEW", menuView);
        menuView.setMnemonic('V');
        JMenuItem database = new JMenuItem(I18n.valueByKey("DATABASE_TAB"), HelperUi.DATABASE_SERVER_ICON);
        I18n.addComponentForKey("DATABASE_TAB", database);
        menuView.add(database);
        JMenuItem adminPage = new JMenuItem(I18n.valueByKey("ADMINPAGE_TAB"), HelperUi.ADMIN_SERVER_ICON);
        I18n.addComponentForKey("ADMINPAGE_TAB", adminPage);
        menuView.add(adminPage);
        JMenuItem file = new JMenuItem(I18n.valueByKey("FILE_TAB"), HelperUi.FILE_SERVER_ICON);
        I18n.addComponentForKey("FILE_TAB", file);
        menuView.add(file);
        JMenuItem webshell = new JMenuItem(I18n.valueByKey("WEBSHELL_TAB"), HelperUi.SHELL_SERVER_ICON);
        I18n.addComponentForKey("WEBSHELL_TAB", webshell);
        menuView.add(webshell);
        JMenuItem sqlshell = new JMenuItem(I18n.valueByKey("SQLSHELL_TAB"), HelperUi.SHELL_SERVER_ICON);
        I18n.addComponentForKey("SQLSHELL_TAB", sqlshell);
        menuView.add(sqlshell);
        JMenuItem upload = new JMenuItem(I18n.valueByKey("UPLOAD_TAB"), HelperUi.UPLOAD_ICON);
        I18n.addComponentForKey("UPLOAD_TAB", upload);
        menuView.add(upload);
        JMenuItem bruteforce = new JMenuItem(I18n.valueByKey("BRUTEFORCE_TAB"), HelperUi.BRUTER_ICON);
        I18n.addComponentForKey("BRUTEFORCE_TAB", bruteforce);
        menuView.add(bruteforce);
        JMenuItem coder = new JMenuItem(I18n.valueByKey("CODER_TAB"), HelperUi.CODER_ICON);
        I18n.addComponentForKey("CODER_TAB", coder);
        menuView.add(coder);
        JMenuItem scanList = new JMenuItem(I18n.valueByKey("SCANLIST_TAB"), HelperUi.SCANLIST_ICON);
        I18n.addComponentForKey("SCANLIST_TAB", scanList);
        menuView.add(scanList);
        menuWindows.add(menuView);

        Preferences prefs = Preferences.userRoot().node(InjectionModel.class.getName());

        JMenu menuPanel = new JMenu(I18n.valueByKey("MENUBAR_PANEL"));
        I18n.addComponentForKey("MENUBAR_PANEL", menuPanel);
        menuView.setMnemonic('V');
        chunkMenu = new JCheckBoxMenuItem(
            I18n.valueByKey("CONSOLE_CHUNK_LABEL"), 
            HelperUi.CHUNK_ICON, 
            prefs.getBoolean(HelperUi.CHUNK_VISIBLE, true)
        );
        I18n.addComponentForKey("CONSOLE_CHUNK_LABEL", chunkMenu);
        menuPanel.add(chunkMenu);
        binaryMenu = new JCheckBoxMenuItem(
            I18n.valueByKey("CONSOLE_BINARY_LABEL"), 
            HelperUi.BINARY_ICON, 
            prefs.getBoolean(HelperUi.BINARY_VISIBLE, true)
        );
        I18n.addComponentForKey("CONSOLE_BINARY_LABEL", binaryMenu);
        menuPanel.add(binaryMenu);
        networkMenu = new JCheckBoxMenuItem(
            I18n.valueByKey("CONSOLE_NETWORK_LABEL"), 
            HelperUi.HEADER_ICON, 
            prefs.getBoolean(HelperUi.NETWORK_VISIBLE, true)
        );
        I18n.addComponentForKey("CONSOLE_NETWORK_LABEL", networkMenu);
        menuPanel.add(networkMenu);
        javaDebugMenu = new JCheckBoxMenuItem(
            I18n.valueByKey("CONSOLE_JAVA_LABEL"), 
            HelperUi.CUP_ICON, 
            prefs.getBoolean(HelperUi.JAVA_VISIBLE, false)
        );
        I18n.addComponentForKey("CONSOLE_JAVA_LABEL", javaDebugMenu);

        for (JCheckBoxMenuItem menuItem: new JCheckBoxMenuItem[]{chunkMenu, binaryMenu, networkMenu, javaDebugMenu}) {
            menuItem.setUI(
                new BasicCheckBoxMenuItemUI() {
                    @Override
                    protected void doClick(MenuSelectionManager msm) {
                        this.menuItem.doClick(0);
                    }
                }
            );
        }

        chunkMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chunkMenu.isSelected()) {
                    MediatorGui.panelConsoles().insertChunkTab();
                } else {
                    // Works even with i18n label
                    MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab("Chunk"));
                }
            }
        });
        binaryMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (binaryMenu.isSelected()) {
                    MediatorGui.panelConsoles().insertBinaryTab();
                } else {
                    // Works even with i18n label
                    MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab("Binary"));
                }
            }
        });
        networkMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (networkMenu.isSelected()) {
                    MediatorGui.panelConsoles().insertNetworkTab();
                } else {
                    // Works even with i18n label
                    MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab("Network"));
                }
            }
        });
        javaDebugMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (javaDebugMenu.isSelected()) {
                    MediatorGui.panelConsoles().insertJavaDebugTab();
                } else {
                    // Works even with i18n label
                    MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab("Java"));
                }
            }
        });

        menuPanel.add(javaDebugMenu);
        menuWindows.add(menuPanel);
        menuWindows.add(new JSeparator());

        database.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
        adminPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
        file.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        webshell.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK));
        sqlshell.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.CTRL_MASK));
        upload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.CTRL_MASK));
        bruteforce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, ActionEvent.CTRL_MASK));
        coder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ActionEvent.CTRL_MASK));
        scanList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.CTRL_MASK));

        final Map<JMenuItem, Integer> mapMenuItem = new HashMap<>();
        mapMenuItem.put(database, 0);
        mapMenuItem.put(adminPage, 1);
        mapMenuItem.put(file, 2);
        mapMenuItem.put(webshell, 3);
        mapMenuItem.put(sqlshell, 4);
        mapMenuItem.put(upload, 5);
        mapMenuItem.put(bruteforce, 6);
        mapMenuItem.put(coder, 7);
        mapMenuItem.put(scanList, 8);
        for (final Entry<JMenuItem, Integer> entry: mapMenuItem.entrySet()) {
            entry.getKey().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    MediatorGui.tabManagers().setSelectedIndex(entry.getValue());
                }
            });
        }

        JMenuItem preferences = new JMenuItem(I18n.valueByKey("MENUBAR_PREFERENCES"), 'P');
        preferences.setIcon(HelperUi.EMPTY);
        I18n.addComponentForKey("MENUBAR_PREFERENCES", preferences);
        
        // Render the Preferences dialog behind scene
        final DialogPreferences dialoguePreferences = new DialogPreferences();
        preferences.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Center the dialog
                if (!dialoguePreferences.isVisible()) {
                    dialoguePreferences.setSize(dialoguePreferences.getWidthDialog(), dialoguePreferences.getHeightDialog());
                    dialoguePreferences.setLocationRelativeTo(MediatorGui.frame());
                    // needed here for button focus
                    dialoguePreferences.setVisible(true);
                    dialoguePreferences.requestButtonFocus();
                }
                dialoguePreferences.setVisible(true);
            }
        });
        menuWindows.add(preferences);

        // Help Menu > about
        JMenu menuHelp = new JMenu(I18n.valueByKey("MENUBAR_HELP"));
        menuHelp.setMnemonic('H');
        I18n.addComponentForKey("MENUBAR_HELP", menuHelp);
        JMenuItem itemHelp = new JMenuItem(I18n.valueByKey("MENUBAR_HELP_ABOUT"), 'A');
        itemHelp.setIcon(HelperUi.EMPTY);
        I18n.addComponentForKey("MENUBAR_HELP_ABOUT", itemHelp);
        JMenuItem itemUpdate = new JMenuItem(I18n.valueByKey("MENUBAR_HELP_UPDATE"), 'U');
        itemUpdate.setIcon(HelperUi.EMPTY);
        I18n.addComponentForKey("MENUBAR_HELP_UPDATE", itemUpdate);

        // Render the About dialog behind scene
        final DialogAbout aboutDiag = new DialogAbout();
        itemHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Center the dialog
                if (!aboutDiag.isVisible()) {
                    aboutDiag.reinit();
                    // needed here for button focus
                    aboutDiag.setVisible(true);
                    aboutDiag.requestButtonFocus();
                }
                aboutDiag.setVisible(true);
            }
        });
        itemUpdate.addActionListener(new ActionCheckUpdate());
        
        menuHelp.add(itemUpdate);
        menuHelp.add(new JSeparator());
        menuHelp.add(itemHelp);

        // Help Menu > about
        JMenu menuCommunity = new JMenu("Community");
        menuHelp.setMnemonic('C');
        JMenuItem itemReportIssue = new JMenuItem(I18n.valueByKey("MENUBAR_COMMUNITY_REPORTISSUE"), 'R');
        itemReportIssue.setIcon(HelperUi.EMPTY);
        I18n.addComponentForKey("MENUBAR_COMMUNITY_REPORTISSUE", itemReportIssue);
        itemReportIssue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JPanel panel = new JPanel(new BorderLayout());
                final JTextArea textarea = new JPopupTextArea(new JTextArea()).getProxy();
                textarea.setText("Username: -\n\nSubject: -\n\nDescription: -");
                panel.add(new JLabel("Describe your issue or the bug you encountered " + ":"), BorderLayout.NORTH);
                panel.add(new LightScrollPane(1, 1, 1, 1, textarea));
                
                panel.setPreferredSize(new Dimension(400, 250));
                panel.setMinimumSize(new Dimension(400, 250));
                
                textarea.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);
                        textarea.requestFocusInWindow();
                    }
                });

                int result = JOptionPane.showOptionDialog(
                    MediatorGui.frame(),
                    panel,
                    "Report an issue or a bug",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Report", I18n.valueByKey("LIST_ADD_VALUE_CANCEL")},
                    I18n.valueByKey("LIST_ADD_VALUE_CANCEL")
                );

                if (!"".equals(textarea.getText()) && result == JOptionPane.YES_OPTION) {
                    GitUtil.sendReport(textarea.getText());
                }
            }
        });
        
        menuCommunity.add(menuI18nContribution);
        menuCommunity.add(new JSeparator());
        menuCommunity.add(itemReportIssue);
        
        // Make menubar
        this.add(menuFile);
        this.add(menuEdit);
        this.add(menuCommunity);
        this.add(menuWindows);
        this.add(menuHelp);
    }
    
    public void switchLocale() {
        for (String key: I18n.keys()) {
            for (Object componentSwing: I18n.componentsByKey(key)) {
                Class<?> classComponent = componentSwing.getClass();
                try {
                    if (componentSwing instanceof JTextFieldPlaceholder) {
                        Method setPlaceholderText = classComponent.getMethod("setPlaceholderText", new Class<?>[]{String.class});
                        setPlaceholderText.invoke(componentSwing, I18n.valueByKey(key));
                    } else {
                        Method methodSetText = classComponent.getMethod("setText", new Class<?>[]{String.class});
                        methodSetText.invoke(componentSwing, I18n.valueByKey(key));
                    }
                } catch (
                    NoSuchMethodException | SecurityException | IllegalAccessException | 
                    IllegalArgumentException | InvocationTargetException e
                ) {
                    LOGGER.warn("Reflection for "+ key +" failed while switching locale", e);
                }
            }
        }
        
        // Fix glitches on Linux
        MediatorGui.frame().revalidate();
    }
}
