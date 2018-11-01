package application;

public class User {
	
	private String username, phone;
	public User()
	{
		setUsername(setPhone(""));	
	}
	public User(String username, String phone)
	{
		this.setPhone(phone);
		this.setUsername(username);
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPhone() {
		return phone;
	}
	public String setPhone(String phone) {
		this.phone = phone;
		return phone;
	}

}
