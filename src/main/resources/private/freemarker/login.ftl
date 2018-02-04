<#include "header.ftl">

<h1>Log In</h1>

<div class="login">
    <form action="/login" method="post">
        <br>
        <label><b>Username</b></label>
        <br>
        <input type="text" placeholder="Enter Username" name="username" required>
        <br>
        <label><b>Password</b></label>
        <br>
        <input id="passInput" type="password" placeholder="Enter Password" name="pwd" required style="float: left;">
        <div style="clear:both;">&nbsp;</div>
        <button type="submit">Let's go</button>
    </form>
</div>
<br>
<a href="/register">Don't have an account?</a>
