package me.baldo3000.rx;

import io.reactivex.rxjava4.disposables.Disposable;
import me.baldo3000.common.api.FSReport;
import me.baldo3000.rx.api.RxFSStat;
import me.baldo3000.rx.impl.RxFSStatImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class RxWithGUI {
    public static class FSReportPresenter {

        private final RxFSStat fsStat = new RxFSStatImpl();
        private Disposable disposable;

        /**
         * Cancels any running scan and starts a new one.
         */
        public void start(Path path, Consumer<FSReport> onReport, Runnable onComplete) {
            stop();
            disposable = fsStat.getFSReport(path, 100_000L, 10)
                    .throttleLatest(500, TimeUnit.MILLISECONDS, true)
                    .doOnNext(r -> log(r.toString())) // Debugging
                    .subscribe(
                            onReport::accept,
                            error -> log("Error: " + error.getMessage()),
                            onComplete::run
                    );
        }

        /**
         * Cancels the running scan, if any.
         */
        public void stop() {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            disposable = null;
        }

        private void log(String msg) {
            IO.println("[" + Thread.currentThread() + "] " + msg);
        }
    }

    public static class MyFrame extends JFrame {

        private final FSReportPresenter presenter = new FSReportPresenter();

        public MyFrame() {
            super("Swing + RxJava");

            JTextArea textArea = new JTextArea(10, 80);
            textArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(textArea);

            JTextField inputField = new JTextField(40);

            JButton startButton = new JButton("Generate FS Report");
            JButton stopButton = new JButton("Stop");
            JButton browseButton = new JButton("Browse...");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            buttonPanel.add(startButton);
            buttonPanel.add(stopButton);
            buttonPanel.add(browseButton);

            JPanel southPanel = new JPanel(new BorderLayout(5, 5));
            southPanel.add(inputField, BorderLayout.NORTH);
            southPanel.add(buttonPanel, BorderLayout.SOUTH);

            getContentPane().setLayout(new BorderLayout(8, 8));
            getContentPane().add(scroll, BorderLayout.CENTER);
            getContentPane().add(southPanel, BorderLayout.SOUTH);

            startButton.addActionListener(_ -> {
                String input = inputField.getText();
                if (input != null && !input.isBlank()) {
                    textArea.setText("");
                    presenter.start(Path.of(input),
                            report -> SwingUtilities.invokeLater(() ->
                                    textArea.setText(report.toString())
                            ),
                            () -> SwingUtilities.invokeLater(() -> {
                                var previous = textArea.getText();
                                textArea.setText(previous + "\nDone");
                            })
                    );
                }
            });

            stopButton.addActionListener(_ -> presenter.stop());

            browseButton.addActionListener(_ -> {
                JFileChooser chooser = getJFileChooser(inputField);
                int ret = chooser.showOpenDialog(this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    if (selected != null) {
                        inputField.setText(selected.getAbsolutePath());
                    }
                }
            });

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
        }

        private JFileChooser getJFileChooser(JTextField inputField) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select directory");
            String current = inputField.getText();
            if (current != null && !current.isBlank()) {
                File f = new File(current);
                if (f.exists() && f.isDirectory()) {
                    chooser.setCurrentDirectory(f);
                }
            }
            return chooser;
        }
    }

    static void main() {
        IO.println("Hello World!");
        SwingUtilities.invokeLater(() -> new MyFrame().setVisible(true));
    }
}
