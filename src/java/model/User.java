package model;

/**
 * Model ánh xạ bảng users trong DB.
 * role: ADMIN | LIBRARIAN | READER
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String studentId;
    private String avatar;
    private String role;
    private int active; // 1 = active, 0 = inactive

    public User() {}

    public User(int id, String username, String fullName, String email,
                String phone, String studentId, String avatar, String role, int active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.studentId = studentId;
        this.avatar = avatar;
        this.role = role;
        this.active = active;
    }

    // ---- Getters & Setters ----
    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getUsername()                    { return username; }
    public void setUsername(String username)       { this.username = username; }

    public String getPassword()                    { return password; }
    public void setPassword(String password)       { this.password = password; }

    public String getFullName()                    { return fullName; }
    public void setFullName(String fullName)       { this.fullName = fullName; }

    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }

    public String getPhone()                       { return phone; }
    public void setPhone(String phone)             { this.phone = phone; }

    public String getStudentId()                   { return studentId; }
    public void setStudentId(String studentId)     { this.studentId = studentId; }

    public String getAvatar()                      { return avatar; }
    public void setAvatar(String avatar)           { this.avatar = avatar; }

    public String getRole()                        { return role; }
    public void setRole(String role)               { this.role = role; }

    public int getActive()                         { return active; }
    public void setActive(int active)              { this.active = active; }

    /** Tiện ích kiểm tra quyền */
    public boolean isAdmin()      { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isLibrarian()  { return "LIBRARIAN".equalsIgnoreCase(role); }
    public boolean isReader()     { return "READER".equalsIgnoreCase(role); }
    public boolean isAdminOrLibrarian() { return isAdmin() || isLibrarian(); }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
