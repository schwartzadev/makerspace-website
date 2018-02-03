<#include "header.ftl">

<h1>Sign up!</h1>
<div class="container">
    <script type="text/javascript" src="/login.js"></script>
    <form action="/signup" method="post" onsubmit="return checkRegister()">
        <label><b>First Name</b></label>
        <br>
        <input id="first" type="text" placeholder="John" name="first" required>
        <br>
        <label><b>Last Name</b></label>
        <br>
        <input id="last" type="text" placeholder="Smith" name="last" required>
        <br>
        <label><b>Username (4+ chars)</b></label>
        <br>
        <input id="username" type="text" placeholder="johnny200" name="username" required>
        <br>
        <label><b>Email</b></label>
        <br>
        <input id="email" type="text" placeholder="johnnys200@jon.com" name="email" required>
        <br>
        <label><b>Password (6+ chars)</b></label>
        <br>
        <input type="password" placeholder="Enter Password" name="pwd" id="first-pass" required>
        <br>
        <label><b>Confirm Password</b></label>
        <br>
        <input type="password" placeholder="Re-Enter Password" name="pwd2" id="second-pass" required>
        <br>
        <span id="message" style="color: #E34234;"></span>
        <br>
        <input type="submit" value="Sign me up">
    </form>
</div>
<br>
<a href="/login">Already have an account?</a>