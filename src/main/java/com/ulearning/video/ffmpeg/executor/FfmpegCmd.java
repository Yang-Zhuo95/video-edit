package com.ulearning.video.ffmpeg.executor;

import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.process.ProcessKiller;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class FfmpegCmd {

    /** The process representing the ffmpeg execution. */
    private Process ffmpeg = null;

    /**
     * A process killer to kill the ffmpeg process with a shutdown hook, useful if the jvm execution
     * is shutted down during an ongoing encoding process.
     */
    private ProcessKiller ffmpegKiller = null;

    /** A stream reading from the ffmpeg process standard output channel. */
    private InputStream inputStream = null;

    /** A stream writing in the ffmpeg process standard input channel. */
    private OutputStream outputStream = null;

    /** A stream reading from the ffmpeg process standard error channel. */
    private InputStream errorStream = null;

    private String os = System.getProperty("os.name");

    /**
     * Executes the ffmpeg process with the previous given arguments.
     *
     * @param destroyOnRuntimeShutdown destroy process if the runtime VM is shutdown
     * @param openIOStreams Open IO streams for input/output and errorout, should be false when
     *     destroyOnRuntimeShutdown is false too
     * @param ffmpegCmd
     * @throws IOException If the process call fails.
     */
    public void execute(boolean destroyOnRuntimeShutdown, boolean openIOStreams, String ffmpegCmd) throws IOException {
        DefaultFFMPEGLocator defaultFFMPEGLocator = new DefaultFFMPEGLocator();

        StringBuilder cmd = new StringBuilder(defaultFFMPEGLocator.getExecutablePath());
        cmd.append(ffmpegCmd);
        String cmdStr = String.format("ffmpegCmd final is :%s", cmd.toString());
        log.info(cmdStr);

        Runtime runtime = Runtime.getRuntime();
        try {
            if (os.toLowerCase().contains("linux")) {
                ffmpeg = runtime.exec(new String[]{"sh","-c",cmd.toString().replaceAll("\n","")});
            } else if (os.toLowerCase().contains("windows")) {
                ffmpeg = runtime.exec(cmd.toString());
            } else {
                log.error("不支持的操作系统：{}", os);
                return;
            }
            if (destroyOnRuntimeShutdown) {
                ffmpegKiller = new ProcessKiller(ffmpeg);
                runtime.addShutdownHook(ffmpegKiller);
            }

            if (openIOStreams) {
                inputStream = ffmpeg.getInputStream();
                outputStream = ffmpeg.getOutputStream();
                errorStream = ffmpeg.getErrorStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a stream reading from the ffmpeg process standard output channel.
     *
     * @return A stream reading from the ffmpeg process standard output channel.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns a stream writing in the ffmpeg process standard input channel.
     *
     * @return A stream writing in the ffmpeg process standard input channel.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns a stream reading from the ffmpeg process standard error channel.
     *
     * @return A stream reading from the ffmpeg process standard error channel.
     */
    public InputStream getErrorStream() {
        return errorStream;
    }

    /** If there's a ffmpeg execution in progress, it kills it. */
    public void destroy() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable t) {
                log.warn("Error closing input stream", t);
            }
            inputStream = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable t) {
                log.warn("Error closing output stream", t);
            }
            outputStream = null;
        }

        if (errorStream != null) {
            try {
                errorStream.close();
            } catch (Throwable t) {
                log.warn("Error closing error stream", t);
            }
            errorStream = null;
        }

        if (ffmpeg != null) {
            ffmpeg.destroy();
            ffmpeg = null;
        }

        if (ffmpegKiller != null) {
            Runtime runtime = Runtime.getRuntime();
            runtime.removeShutdownHook(ffmpegKiller);
            ffmpegKiller = null;
        }
    }

    /**
     * Return the exit code of the ffmpeg process If the process is not yet terminated, it waits for
     * the termination of the process
     *
     * @return process exit code
     */
    public int getProcessExitCode() {
        // Make sure it's terminated
        try {
            ffmpeg.waitFor();
        } catch (InterruptedException ex) {
            log.warn("Interrupted during waiting on process, forced shutdown?", ex);
        }
        return ffmpeg.exitValue();
    }

    /**close**/
    public void close() {
        destroy();
    }

}
