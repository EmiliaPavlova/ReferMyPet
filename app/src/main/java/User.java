import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    public static final String TYPE_HOTEL = "HOTEL";
    public static final String TYPE_REGULAR = "REGULAR";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "hotel_name")
    public String hotelName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "user_type")
    public String userType;

    public User(String hotelName, String email, String passwordHash) {
        this.hotelName = hotelName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = TYPE_HOTEL;
    }

    public int getId() { return id; }
    public String getHotelName() { return hotelName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getUserType() { return userType; }
}
