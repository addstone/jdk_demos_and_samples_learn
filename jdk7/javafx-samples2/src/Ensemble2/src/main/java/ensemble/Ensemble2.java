package ensemble;

import ensemble.config.ProxyDialog;
import ensemble.controls.BreadcrumbBar;
import ensemble.controls.SearchBox;
import ensemble.controls.WindowButtons;
import ensemble.controls.WindowResizeButton;
import ensemble.pages.SamplePage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import netscape.javascript.JSObject;

/**
 * Description:  Ensemble Main Application
 * Description:  Ensemble2 主程序
 * Author: LuDaShi
 * Date: 2020-12-31-12:29
 * UpdateDate: 2020-12-31-12:29
 * FileName: Ensemble2
 * Version: 0.0.0.1
 * Since: 0.0.0.1
 */
public class Ensemble2 extends Application {

    static {
        // Enable using system proxy if set
        // 如果设置，启用使用系统代理
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public static final String DEFAULT_DOCS_URL = "http://download.oracle.com/javafx/2/api/";

    private static Ensemble2 ensemble2;
    private static boolean isApplet = false;
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private ToolBar toolBar;
    private SplitPane splitPane;
    private TreeView pageTree;
    private Pane pageArea;
    private Pages pages;
    private Page currentPage;
    private String currentPagePath;
    private Node currentPageView;
    private BreadcrumbBar breadcrumbBar;
    private Stack<Page> history = new Stack<Page>();
    private Stack<Page> forwardHistory = new Stack<Page>();
    private boolean changingPage = false;
    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetY = 0;
    private WindowResizeButton windowResizeButton;
    public boolean fromForwardOrBackButton = false;
    private StackPane modalDimmer;
    private ProxyDialog proxyDialog;
    private ToolBar pageToolBar;
    private JSObject browser;
    private String docsUrl;
    // 获取开始时间
    long startTime = System.currentTimeMillis();


    /**
     * Get the singleton instance of Ensemble
     * 获取Ensemble的单例实例
     *
     * @return The singleton instance
     */
    public static Ensemble2 getEnsemble2() {
        return ensemble2;
    }

    /**
     * Start the application
     * 启动应用程序
     *
     * @param stage The main application stage
     */
    @Override
    public void start(final Stage stage) {
        ensemble2 = this;
        stage.setTitle("Ensemble2");
        // 设置默认文档位置
        docsUrl = System.getProperty("docs.url") != null ? System.getProperty("docs.url") : DEFAULT_DOCS_URL;
        // 创建根堆栈窗格中,我们使用能够覆盖代理对话框
        StackPane layerPane = new StackPane();
        // 这段 isApplet 在jdk11 应该不需要
        // 检查是否 applet
        try {
            browser = getHostServices().getWebContext();
            isApplet = browser != null;
        } catch (Exception e) {
            isApplet = false;
        }
        if (!isApplet) {
            stage.initStyle(StageStyle.UNDECORATED);
            // 创建窗口调整大小按钮
            windowResizeButton = new WindowResizeButton(stage, 1020, 700);
            // 创建 root
            root = new BorderPane() {
                @Override
                protected void layoutChildren() {
                    super.layoutChildren();
                    windowResizeButton.autosize();
                    windowResizeButton.setLayoutX(getWidth() - windowResizeButton.getLayoutBounds().getWidth());
                    windowResizeButton.setLayoutY(getHeight() - windowResizeButton.getLayoutBounds().getHeight());
                }
            };
            root.getStyleClass().add("application");
        } else {
            root = new BorderPane();
            root.getStyleClass().add("applet");
        }
        root.setId("root");
        layerPane.setDepthTest(DepthTest.DISABLE);
        layerPane.getChildren().add(root);
        // create scene
        // 创建场景
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        scene = new Scene(layerPane, 1020, 700, is3dSupported);
        if (is3dSupported) {
            //RT-13234
            scene.setCamera(new PerspectiveCamera());
        }
        scene.getStylesheets().add(Ensemble2.class.getResource("ensemble2.css").toExternalForm());
        // 创建模式调光器，以在显示模式对话框时使屏幕变暗
        modalDimmer = new StackPane();
        modalDimmer.setId("ModalDimmer");
        modalDimmer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                t.consume();
                hideModalMessage();
            }
        });
        modalDimmer.setVisible(false);
        layerPane.getChildren().add(modalDimmer);
        // 创建主工具栏
        toolBar = new ToolBar();
        toolBar.setId("mainToolBar");
        ImageView logo = new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/logo.png")));
        HBox.setMargin(logo, new Insets(0, 0, 0, 5));
        toolBar.getItems().add(logo);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolBar.getItems().add(spacer);
        Button highlightsButton = new Button();
        highlightsButton.setId("highlightsButton");
        highlightsButton.setMinSize(120, 66);
        highlightsButton.setPrefSize(120, 66);
        highlightsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                goToPage(Pages.HIGHLIGHTS);
            }
        });
        toolBar.getItems().add(highlightsButton);
        Button newButton = new Button();
        newButton.setId("newButton");
        newButton.setMinSize(120, 66);
        newButton.setPrefSize(120, 66);
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                goToPage(Pages.NEW);
            }
        });
        toolBar.getItems().add(newButton);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        toolBar.getItems().add(spacer2);
        ImageView searchTest = new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/search-text.png")));
        toolBar.getItems().add(searchTest);
        SearchBox searchBox = new SearchBox();
        HBox.setMargin(searchBox, new Insets(0, 5, 0, 0));
        toolBar.getItems().add(searchBox);
        toolBar.setPrefHeight(66);
        toolBar.setMinHeight(66);
        toolBar.setMaxHeight(66);
        GridPane.setConstraints(toolBar, 0, 0);
        if (!isApplet) {
            // add close min max
            // 添加关闭最小值最大值
            final WindowButtons windowButtons = new WindowButtons(stage);
            toolBar.getItems().add(windowButtons);
            // add window header double clicking
            // 添加窗口标题双击
            toolBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        windowButtons.toogleMaximized();
                    }
                }
            });
            // add window dragging
            // 添加窗口拖动
            toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mouseDragOffsetX = event.getSceneX();
                    mouseDragOffsetY = event.getSceneY();
                }
            });
            toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (!windowButtons.isMaximized()) {
                        stage.setX(event.getScreenX() - mouseDragOffsetX);
                        stage.setY(event.getScreenY() - mouseDragOffsetY);
                    }
                }
            });
        }
        // 创建页面树工具栏
        ToolBar pageTreeToolBar = new ToolBar() {
            @Override
            public void requestLayout() {
                super.requestLayout();
                // keep the height of pageToolBar in sync with pageTreeToolBar so they always match
                // 保持pageToolBar的高度与pageTreeToolBar同步，以便它们始终匹配
                if (pageToolBar != null && getHeight() != pageToolBar.prefHeight(-1)) {
                    pageToolBar.setPrefHeight(getHeight());
                }
            }
        };
        pageTreeToolBar.setId("page-tree-toolbar");
        pageTreeToolBar.setMinHeight(29);
        pageTreeToolBar.setMaxWidth(Double.MAX_VALUE);
        ToggleGroup pageButtonGroup = new ToggleGroup();
        final ToggleButton allButton = new ToggleButton("All");
        allButton.setToggleGroup(pageButtonGroup);
        allButton.setSelected(true);
        final ToggleButton samplesButton = new ToggleButton("Samples");
        samplesButton.setToggleGroup(pageButtonGroup);
        final ToggleButton docsButton = new ToggleButton("Document");
        docsButton.setToggleGroup(pageButtonGroup);
        InvalidationListener treeButtonNotifyListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                if (allButton.isSelected()) {
                    pageTree.setRoot(pages.getRoot());
                } else if (samplesButton.isSelected()) {
                    pageTree.setRoot(pages.getSamples());
                } else if (docsButton.isSelected()) {
                    pageTree.setRoot(pages.getDocs());
                }
            }
        };
        allButton.selectedProperty().addListener(treeButtonNotifyListener);
        samplesButton.selectedProperty().addListener(treeButtonNotifyListener);
        docsButton.selectedProperty().addListener(treeButtonNotifyListener);
        pageTreeToolBar.getItems().addAll(allButton, samplesButton, docsButton);
        // create page tree
        // 创建页面树
        pages = new Pages();
        proxyDialog = new ProxyDialog(stage, pages);
        proxyDialog.loadSettings();
        proxyDialog.getDocsInBackground(true, null);
        pages.parseSamples();
        pageTree = new TreeView();
        pageTree.setId("page-tree");
        pageTree.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pageTree.setRoot(pages.getRoot());
        pageTree.setShowRoot(false);
        pageTree.setEditable(false);
        pageTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        pageTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                Object newValue) {
                if (!changingPage) {
                    Page selectedPage = (Page) pageTree.getSelectionModel().getSelectedItem();
                    if (selectedPage != pages.getRoot()) {
                        goToPage(selectedPage);
                    }
                }
            }
        });
        // create left split pane
        // 创建左拆分窗格
        BorderPane leftSplitPane = new BorderPane();
        leftSplitPane.setTop(pageTreeToolBar);
        leftSplitPane.setCenter(pageTree);
        // create page toolbar
        // 创建页面工具栏
        pageToolBar = new ToolBar();
        pageToolBar.setId("page-toolbar");
        pageToolBar.setMinHeight(29);
        pageToolBar.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
        if (!isApplet) {
            Button backButton = new Button();
            backButton.setGraphic(new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/back.png"))));
            backButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    back();
                }
            });
            backButton.setMaxHeight(Double.MAX_VALUE);
            Button forwardButton = new Button();
            forwardButton.setGraphic(new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/forward.png"))));
            forwardButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    forward();
                }
            });
            forwardButton.setMaxHeight(Double.MAX_VALUE);
            Button reloadButton = new Button();
            reloadButton.setGraphic(new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/reload.png"))));
            reloadButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reload();
                }
            });
            reloadButton.setMaxHeight(Double.MAX_VALUE);
            pageToolBar.getItems().addAll(backButton, forwardButton, reloadButton);
        }
        breadcrumbBar = new BreadcrumbBar();
        pageToolBar.getItems().add(breadcrumbBar);
        if (!isApplet) {
            Region spacer3 = new Region();
            HBox.setHgrow(spacer3, Priority.ALWAYS);
            Button settingsButton = new Button();
            settingsButton.setId("SettingsButton");
            settingsButton.setGraphic(new ImageView(new Image(Ensemble2.class.getResourceAsStream("images/settings.png"))));
            settingsButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    showProxyDialog();
                }
            });
            settingsButton.setMaxHeight(Double.MAX_VALUE);
            pageToolBar.getItems().addAll(spacer3, settingsButton);
        }
        // create page area
        // 创建页面区域
        pageArea = new Pane() {
            @Override
            protected void layoutChildren() {
                for (Node child : pageArea.getChildren()) {
                    child.resizeRelocate(0, 0, pageArea.getWidth(), pageArea.getHeight());
                }
            }
        };
        pageArea.setId("page-area");
        // create right split pane
        // 创建右分割窗格
        BorderPane rightSplitPane = new BorderPane();
        rightSplitPane.setTop(pageToolBar);
        rightSplitPane.setCenter(pageArea);
        // create split pane
        // 创建拆分窗格
        splitPane = new SplitPane();
        splitPane.setId("page-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setConstraints(splitPane, 0, 1);
        splitPane.getItems().addAll(leftSplitPane, rightSplitPane);
        splitPane.setDividerPosition(0, 0.25);

        this.root.setTop(toolBar);
        this.root.setCenter(splitPane);
        // add window resize button so its on top
        // 添加窗口调整大小按钮，使其位于顶部
        if (!isApplet) {
            windowResizeButton.setManaged(false);
            this.root.getChildren().add(windowResizeButton);
        }
        // expand first level of the tree
        // 展开树的第一层
        for (TreeItem child : pages.getRoot().getChildren()) {
            if (child == pages.getHighlighted() || child == pages.getNew()) {
                continue;
            }
            child.setExpanded(true);
            for (TreeItem child2 : (ObservableList<TreeItem<String>>) child.getChildren()) {
                child2.setExpanded(true);
            }
        }
        // goto initial page
        // 转到初始页
        if (isApplet) {
            String hashLoc = getBrowserHashLocation();
            if (hashLoc != null) {
                goToPage(hashLoc);
            } else {
                // default to all samples
                goToPage(pages.getSamples());
            }
        } else {
            // default to all samples
            // 默认为所有样本
            goToPage(pages.getSamples());
        }
        // show stage
        // 表演舞台
        stage.setScene(scene);
        stage.show();
        //获取结束时间
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }

    /**
     * Called from JavaScript in the browser when the page hash location changes
     * 页面哈希位置更改时在浏览器中从JavaScript调用
     *
     * @param hashLoc The new has location, e.g. #SAMPLES
     */
    public void hashChanged(String hashLoc) {
        if (hashLoc != null) {
            // remove #
            if (hashLoc.length() == 0) {
                hashLoc = null;
            } else {
                hashLoc = hashLoc.substring(1);
            }
            // if new page then navigate to it
            final String path = hashLoc;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (!path.equals(currentPagePath)) {
                        goToPage(path);
                    }
                }
            });
        }
    }

    /**
     * Get the URL of the java doc root directory being used to get
     * documentation from
     *
     * @return Documentation directory URL
     */
    public String getDocsUrl() {
        return docsUrl;
    }

    /**
     * Set the URL of the java doc root directory being used to get
     * documentation from
     *
     * @param docsUrl Documentation directory URL
     */
    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl;
    }

    /**
     * Fetch the current hash location from the browser via JavaScript
     *
     * @return Current browsers hash location
     */
    private String getBrowserHashLocation() {
        String hashLoc = null;
        try {
            hashLoc = (String) browser.eval("window.location.hash");
        } catch (Exception e) {
            try {
                System.out.println("Warning failed to get browser location, retrying...");
                hashLoc = (String) browser.eval("window.location.hash");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        // remove #
        if (hashLoc != null) {
            if (hashLoc.length() == 0) {
                hashLoc = null;
            } else {
                hashLoc = hashLoc.substring(1);
            }
        }
        return hashLoc;
    }

    /**
     * Show the given node as a floating dialog over the whole application, with
     * the rest of the application dimmed out and blocked from mouse events.
     *
     * @param message
     */
    public void showModalMessage(Node message) {
        modalDimmer.getChildren().add(message);
        modalDimmer.setOpacity(0);
        modalDimmer.setVisible(true);
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(
            new KeyFrame(Duration.seconds(1),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        modalDimmer.setCache(false);
                    }
                },
                new KeyValue(modalDimmer.opacityProperty(), 1, Interpolator.EASE_BOTH)
            )).build().play();
    }

    /**
     * Hide any modal message that is shown
     */
    public void hideModalMessage() {
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(
            new KeyFrame(Duration.seconds(1),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        modalDimmer.setCache(false);
                        modalDimmer.setVisible(false);
                        modalDimmer.getChildren().clear();
                    }
                },
                new KeyValue(modalDimmer.opacityProperty(), 0, Interpolator.EASE_BOTH)
            )).build().play();
    }

    /**
     * Get the pages object that contains the tree of all avaliable pages
     *
     * @return Pages containing tree of all pages
     */
    public Pages getPages() {
        return pages;
    }

    /**
     * Change to new page without swapping views, assumes that the current view
     * is already showing the new page
     *
     * @param page The new page object
     */
    public void updateCurrentPage(Page page) {
        goToPage(page, true, false, false);
    }

    /**
     * Take ensemble to the given page path, navigating there and adding
     * current page to history
     *
     * @param pagePath The path for the new page to show
     */
    public void goToPage(String pagePath) {
        goToPage(pages.getPage(pagePath));
    }

    /**
     * Take ensemble to the given page path, navigating there and adding
     * current page to history.
     *
     * @param pagePath  The path for the new page to show
     * @param force     Reload page even if its the current page
     */
    public void goToPage(String pagePath, boolean force) {
        goToPage(pages.getPage(pagePath), true, force, true);
    }

    /**
     * Take ensemble to the given page object, navigating there and adding
     * current page to history.
     *
     * @param page Page object to show
     */
    public void goToPage(Page page) {
        goToPage(page, true, false, true);
    }

    /**
     * Take ensemble to the given page object, navigating there.
     *
     * @param page          Page object to show
     * @param addHistory    When true add current page to history before navigating
     * @param force         When true reload page if page is current page
     * @param swapViews     If view should be swapped to new page
     */
    private void goToPage(Page page, boolean addHistory, boolean force, boolean swapViews) {
        if (page == null) {
            return;
        }
        if (!force && page == currentPage) {
            return;
        }
        changingPage = true;
        if (swapViews) {
            Node view = page.createView();
            if (view == null) {
                view = new Region(); // todo temp workaround
            }
            // replace view in pageArea if new
            if (force || view != currentPageView) {
                for (Node child : pageArea.getChildren()) {
                    if (child instanceof SamplePage.SamplePageView) {
                        ((SamplePage.SamplePageView) child).stop();
                    }
                }
                pageArea.getChildren().setAll(view);
                currentPageView = view;
            }
        }
        // add page to history
        if (addHistory && currentPage != null) {
            history.push(currentPage);
            forwardHistory.clear();
        }
        currentPage = page;
        currentPagePath = page.getPath();
        // when in applet update location bar
        if (isApplet) {
            try {
                browser.eval("window.location.hash='" + currentPagePath + "';");
            } catch (Exception e) {
                try {
                    System.out.println("Warning failed to set browser location, retrying...");
                    browser.eval("window.location.hash='" + currentPagePath + "';");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        // expand tree to selected node
        Page p = page;
        while (p != null) {
            p.setExpanded(true);
            p = (Page) p.getParent();
        }
        // update tree selection
        pageTree.getSelectionModel().select(page);
        // update breadcrumb bar
        breadcrumbBar.setPath(currentPagePath);
        // done
        changingPage = false;
    }

    /**
     * Check if current call stack was from back or forward button's action
     *
     * @return True if current call was caused by action on back or forward button
     */
    public boolean isFromForwardOrBackButton() {
        return fromForwardOrBackButton;
    }

    /**
     * Got to previous page in history
     */
    public void back() {
        fromForwardOrBackButton = true;
        if (!history.isEmpty()) {
            Page prevPage = history.pop();
            forwardHistory.push(currentPage);
            goToPage(prevPage, false, false, true);
        }
        fromForwardOrBackButton = false;
    }

    /**
     * Utility method for viewing the page history
     */
    private void printHistory() {
        System.out.print("   HISTORY = ");
        for (Page page : history) {
            System.out.print(page.getName() + "->");
        }
        System.out.print("   [" + currentPage.getName() + "]");
        for (Page page : forwardHistory) {
            System.out.print(page.getName() + "->");
        }
        System.out.print("\n");
    }

    /**
     * Go to next page in history if there is one
     */
    public void forward() {
        fromForwardOrBackButton = true;
        if (!forwardHistory.isEmpty()) {
            Page prevPage = forwardHistory.pop();
            history.push(currentPage);
            goToPage(prevPage, false, false, true);
        }
        fromForwardOrBackButton = false;
    }

    /**
     * Reload the current page
     */
    public void reload() {
        goToPage(currentPage, false, true, true);
    }

    /**
     * Show the dialog for setting proxy to the user
     */
    public void showProxyDialog() {
        showModalMessage(proxyDialog);
    }

    /**
     * Java Main Method for launching application when not using JavaFX
     * Launcher, eg from IDE without JavaFX support
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
