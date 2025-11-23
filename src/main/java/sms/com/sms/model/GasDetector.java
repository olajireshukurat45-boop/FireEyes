package sms.com.sms.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "gas_detectors")
public class GasDetector {

    @Id
    @Column(name = "mac_address", nullable = false, unique = true)
    private String macAddress;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Boolean status;

    private Double temperature;
    private Double co2;
    private Double humidity;

    @ManyToMany(mappedBy = "gasDetectors", fetch = FetchType.EAGER)
    private Set<Users> users = new HashSet<>();

    // === Getters and Setters ===
public Double getCo2(){
    return co2;
}
public void setCo2(Double co2){
    this.co2 =co2;
}
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Set<Users> getUsers() {
        return users;
    }

    public void setUsers(Set<Users> users) {
        this.users = users;
    }

    // === Bi-directional management ===

    public void addUser(Users user) {
        this.users.add(user);
        user.getGasDetectors().add(this);
    }

    public void removeUser(Users user) {
        this.users.remove(user);
        user.getGasDetectors().remove(this);
    }

    // === Optional toString method ===
    @Override
    public String toString() {
        return "GasDetector{" +
                "macAddress='" + macAddress + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
    }
    public GasDetector get() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }
}
