package edu.sdsu.cs;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import edu.sdsu.cs.Models.CaptionLine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
@Log4j
public class LineProcessor {
    private static LineProcessor ourInstance = new LineProcessor();

    @Getter(AccessLevel.PACKAGE)
    private MatlabEngine eng = null;

    private LineProcessor() {
        try {
            eng = MatlabEngine.startMatlab();
        } catch (EngineException e) {
            log.fatal("Could not start MATLAB Engine", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.warn("MATLAB connection interrupted", e);
        }
    }

    static LineProcessor getInstance() {
        return ourInstance;
    }

    void processCaptionLine(CaptionLine line) {
        // TODO
    }
}
