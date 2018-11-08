package application;

public class User {
	
	private String username="", phone="";
	public User()
	{
		this.username = this.phone = "";
	}
	public User(String username, String phone)
	{
		this.username = username;
		this.phone = phone;
	}
	public String getUsername() {
		return this.username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPhone() {
		return this.phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;

	}

}
