package com.pattern.acquaintances.model;
public class Account {
    private String firstName;
    private String lastName;
    private DayOfBirth dayOfBirth;
    private String location;
    public  Account(){}
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public DayOfBirth getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(DayOfBirth dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}

