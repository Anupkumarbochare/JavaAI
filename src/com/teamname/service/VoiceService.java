package com.teamname.service;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceService {
    private Voice voice;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean stopRequested = false;
    private volatile boolean isSpeaking = false;
    private boolean ttsAvailable = false;
    private boolean sttAvailable = true;
    private Thread speakingThread;

    public VoiceService() {
        initTTS();
    }

    private void initTTS() {
        try {
            System.setProperty("freetts.voices",
                    "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager vm = VoiceManager.getInstance();
            voice = vm.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                voice.setRate(150);
                voice.setPitch(100);
                voice.setVolume(3.0f);
                ttsAvailable = true;
            } else {
                System.err.println("❌ Could not find 'kevin16' voice.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void speak(String text) {
        if (!ttsAvailable || voice == null) return;

        stopRequested = false;
        isSpeaking = true;

        speakingThread = new Thread(() -> {
            try {
                for (String sentence : text.split("[.!?]")) {
                    if (stopRequested || Thread.currentThread().isInterrupted()) break;
                    String trimmed = sentence.trim();
                    if (!trimmed.isEmpty()) {
                        voice.getAudioPlayer().cancel(); // Stop any queued output
                        voice.speak(trimmed + ".");
                        Thread.sleep(30);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("🛑 Speech interrupted.");
            } catch (Exception e) {
                System.err.println("Speech error: " + e.getMessage());
            } finally {
                isSpeaking = false;
                if (speechListener != null) speechListener.speechStopped();
            }
        });

        speakingThread.start();
    }

    public void stopSpeaking() {
        stopRequested = true;
        if (voice != null && voice.getAudioPlayer() != null) {
            voice.getAudioPlayer().cancel();
        }
        if (speakingThread != null && speakingThread.isAlive()) {
            speakingThread.interrupt();
        }
        isSpeaking = false;
        if (speechListener != null) speechListener.speechStopped();
        System.out.println("🛑 Speech forcefully stopped");
    }


    public interface SpeechListener {
        void speechStopped();
    }

    private SpeechListener speechListener;

    public void setSpeechListener(SpeechListener listener) {
        this.speechListener = listener;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void startListening(java.util.function.Consumer<String> onSpeechReceived) {
        executor.submit(() -> {
            try {
                File html = File.createTempFile("voice_ui", ".html");
                try (PrintWriter writer = new PrintWriter(html)) {
                    writer.println("""
                        <!DOCTYPE html>
                        <html>
                        <body>
                        <h2>🎙️ Speak Now...</h2>
                        <script>
                            const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
                            recognition.lang = 'en-US';
                            recognition.continuous = true;
                            recognition.interimResults = false;

                            recognition.onresult = (event) => {
                                let transcript = "";
                                for (let i = event.resultIndex; i < event.results.length; ++i) {
                                    transcript += event.results[i][0].transcript + " ";
                                }
                                fetch("http://localhost:9876/stt?text=" + encodeURIComponent(transcript.trim()));
                            };

                            recognition.onerror = (event) => {
                                if (event.error !== "aborted") {
                                    setTimeout(() => recognition.start(), 1000);
                                }
                            };

                            recognition.onend = () => {
                                setTimeout(() => recognition.start(), 1000);
                            };

                            recognition.start();
                        </script>
                        </body>
                        </html>
                    """);
                }

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(html.toURI());
                } else {
                    System.out.println("📂 Open manually: " + html.getAbsolutePath());
                }

                new Thread(() -> {
                    try (ServerSocket server = new ServerSocket(9876)) {
                        while (!stopRequested) {
                            Socket client = server.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String line;
                            String text = null;

                            while ((line = in.readLine()) != null && !line.isEmpty()) {
                                if (line.contains("GET /stt?text=")) {
                                    text = URLDecoder.decode(line.split("text=")[1].split(" ")[0], "UTF-8");
                                    break;
                                }
                            }

                            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                            out.println("HTTP/1.1 200 OK\r\n\r\n");
                            out.flush();
                            client.close();

                            if (text != null && !text.trim().isEmpty()) {
                                onSpeechReceived.accept(text);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopListening() {
        stopRequested = true;
        System.out.println("🛑 Listening stopped.");
    }

    public void close() {
        stopRequested = true;
        if (voice != null) voice.deallocate();
        executor.shutdown();
    }

    public boolean isTTSAvailable() {
        return ttsAvailable;
    }

    public boolean isSTTAvailable() {
        return sttAvailable;
    }

}


