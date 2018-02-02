function checkRegister() {
    console.log('in js');
    var user = document.getElementById('username');
    var pass1 = document.getElementById('first-pass');
    var pass2 = document.getElementById('second-pass');
    var message = document.getElementById('message');

    function failedTest(error) {
        message.innerHTML = error;
        message.textContent = error;
        ok = false;
    }

    var ok = true;
    if (pass1.value != pass2.value) {
        pass1.style.borderColor = "#E34234";
        pass2.style.borderColor = "#E34234";
        failedTest("passwords don't match");
    } else {
        if (pass1.value.length < 6) {
            failedTest('your password must contain at least 6 characters');
        }
        if (pass1.value.length >= 255) {
            failedTest('your password must contain less than 255 characters');
        }
        if (user.value.length >= 255) {
            failedTest('your username must contain less than 255 characters');
        }
        if (user.value.length < 4) {
            failedTest('your username must contain at least 4 characters');
        }
    }
    return ok;
}