function validateForm() {
    // Get form input values
    const name = document.getElementById("custname").value.trim();
    const email = document.getElementById("custemail").value.trim();
    const password = document.getElementById("custpassword").value;
    const phoneNumber = document.getElementById("custphoneno").value.trim();

    // Updated Regular expressions for validation
    const nameRegex = /^[a-zA-Z\s]+$/; // Only letters and spaces
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.[A-Z]|.[!@#$%^&()_+\-=\[\]{};':"\\|,.<>\/?])(?=.[a-z0-9]).{7,}$/;
    const phoneNumberRegex = /^[0-9]+$/; // Only numbers

    // Validation checks
    if (!nameRegex.test(name)) {
        alert("Name must contain only letters and spaces.");
        return false;
    }

    if (name.length < 3) {
        alert("Name must be at least 3 characters long.");
        return false;
    }

    if (!emailRegex.test(email)) {
        alert("Please enter a valid email address.");
        return false;
    }

    if (!passwordRegex.test(password)) {
        alert("Password must be at least 7 characters long and contain either an uppercase letter or a special symbol.");
        return false;
    }

    if (!phoneNumberRegex.test(phoneNumber)) {
        alert("Phone number must contain only numbers.");
        return false;
    }

    if (phoneNumber.length < 10 || phoneNumber.length > 11) {
        alert("Phone number must be 10-11 digits long.");
        return false;
    }

    return true;
}
