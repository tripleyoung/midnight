package com.outsider.midnight.user.command.domain.aggregate;

import com.outsider.midnight.user.command.domain.aggregate.embeded.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_user")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 또는 AUTO, SEQUENCE, TABLE 중 하나 선택
    private Long id;
    @Column(name = "user_email")
    private String email;

    @Column(name = "user_password")
    private String password;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_age")
    private int age;

    @Column(name = "user_point")
    private BigDecimal points;

    @Column(name = "user_gender")
    private Gender gender;

    @Column(name = "user_location")
    private Location location;
    @Column(name = "user_withdrawal")
    private Boolean isWithdrawal;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_authority")
    private Authority authority;
    @Embedded
    private Tier tier;
    @Embedded
    private ProviderInfo provider; //어떤 OAuth인지(google, naver 등)
    private String provideId; // 해당 OAuth 의 key(id)
    @Column(name = "user_img")
    private String userImg ;

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public  Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Column(name = "join_date")
    private LocalDate joinDate;

    public Authority getAuthority() {
        return authority;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public void setWithdrawal(Boolean withdrawal) {
        isWithdrawal = withdrawal;
    }

    public Boolean getWithdrawal() {
        return isWithdrawal;
    }

    public User(Boolean isWithdrawal) {
        this.isWithdrawal = isWithdrawal;
    }

    public User() {
    }
    public User(String email, String password, String userName,Authority authority,ProviderInfo provider ) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.tier = new Tier();
        this.points = new BigDecimal(0);
        this.isWithdrawal = false;
        this.authority =authority;
        this.joinDate = LocalDate.now();
        this.provider = provider;
    }
    public User(String email, String password, String userName, int age, Gender gender, Location lcation,Authority authority) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.age = age;
        this.gender = gender;
        this.location = lcation;
        this.tier = new Tier();
        this.points = new BigDecimal(0);
        this.isWithdrawal = false;
        this.joinDate = LocalDate.now();
        this.authority = authority;


    }

    public Long getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public int getAge() {
        return age;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public Gender getGender() {
        return gender;
    }

    public Location getLocation() {
        return location;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPoints(BigDecimal point) {
        this.points = point;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setLocation(Location lcation) {
        this.location = lcation;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", age=" + age +
                ", points=" + points +
                ", gender=" + gender +
                ", lcation=" + location +
                '}';
    }
}
