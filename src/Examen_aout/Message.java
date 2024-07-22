package Examen_aout;

import com.fazecast.jSerialComm.SerialPort;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private String time;

    public Message(String message, String time) {
        this.message = message;
        this.time = time;
    }

    public Message(SerialPort port) {
        this.message = port.getDescriptivePortName();
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Message{" +
                " time='" + time + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
