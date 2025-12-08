package madstodolist.dto;

public class PasswordChangeData {

    private String passwordActual;
    private String passwordNueva;
    private String passwordRepetida;

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }

    public String getPasswordRepetida() {
        return passwordRepetida;
    }

    public void setPasswordRepetida(String passwordRepetida) {
        this.passwordRepetida = passwordRepetida;
    }
}
