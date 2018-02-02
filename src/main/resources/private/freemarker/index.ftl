<#include "header.ftl">
<div id="container">
    <table id="judge-list">
        <thead>
            <td>First</td>
            <td>Last</td>
            <td>Username</td>
            <td>Email</td>
            <td>Latest Login Date</td>
            <td>Add'l Info</td>
        </thead>
        <tbody>
        <#list item1 as user>
            <tr class="link-row" data-href="/profile/${user.getId()}">
                <td>${user.getFirstname()}</td>
                <td>${user.getLastname()}</td>
                <td>${user.getUsername()}</td>
                <td>${user.getEmail()}</td>
                <td>${user.getLoginDate()}</td>
                <td><a href="/user/${user.getId()}">More Details...</a></td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
<#include "footer.ftl">
<script type="text/javascript" src="/main.js"></script>