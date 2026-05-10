package me.baldo3000.rx;

import io.reactivex.rxjava4.disposables.Disposable;
import me.baldo3000.rx.api.RxFSStat;
import me.baldo3000.rx.impl.RxFSStatImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;


public class RxWithGUI {
    static class MyFrame extends JFrame {

        private final RxFSStat fsStat = new RxFSStatImpl();
        private Disposable disposable;

        public MyFrame() {
            super("Swing + RxJava");

            JTextArea textArea = new JTextArea(10, 70);
            textArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(textArea);

            JTextField inputField = new JTextField(40);
            inputField.setText("C:/Users/andre/AppData");

            JButton startButton = new JButton("Generate FS Report");
            JButton stopButton = new JButton("Stop");
            JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 5));
            buttonPanel.add(startButton);
            buttonPanel.add(stopButton);

            JPanel southPanel = new JPanel(new java.awt.BorderLayout(5, 5));
            southPanel.add(inputField, java.awt.BorderLayout.NORTH);
            southPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

            getContentPane().setLayout(new java.awt.BorderLayout(8, 8));
            getContentPane().add(scroll, java.awt.BorderLayout.CENTER);
            getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

            startButton.addActionListener((ActionEvent ev) -> {
                // Always cancel whatever was running before starting fresh.
                stopCurrentComputation();

                String input = inputField.getText();
                log("Start pressed: " + input);

                if (input != null && !input.isBlank()) {
                    textArea.setText("");
                    disposable = fsStat.getFSReport(Path.of(input), 100_000L, 10)
                            .throttleLatest(500, TimeUnit.MILLISECONDS, true).doOnNext(System.out::println)
                            .subscribe(
                                    report -> SwingUtilities.invokeLater(() -> textArea.setText(report.toString()))
                            );
                }
            });

            stopButton.addActionListener((ActionEvent _) -> {
                log("Stop pressed");
                stopCurrentComputation();
            });

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
        }

        private void stopCurrentComputation() {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            disposable = null;
        }
    }

    static void main() {
        IO.println("Hello World!");
        SwingUtilities.invokeLater(() -> new MyFrame().setVisible(true));
    }

    private static void log(String msg) {
        IO.println("[" + Thread.currentThread() + "] " + msg);
    }
}
