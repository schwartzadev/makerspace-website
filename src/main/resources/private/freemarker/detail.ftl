<#include "header.ftl">

<div id="container">
    <div id="wrapper">
        <h1 class="name"><span>${judge.getFirstname()}</span> <span>${judge.getLastname()}</span></h1>
        <div id="floating">
            <div id="sidebar">
                <div class="one-half column"><p class="counter">Speed: <span>${judge.getSpeed()}</span></p></div>
                <div class="one-half column"><p class="counter">Flows: <span>${judge.getFlowing()}</span></p></div>
            </div>
        </div>
    </div>

    <div class="center toolbar">
        <ul>
            <li><a href="/edit/${judge.getId()}">edit profile</a></li>
            <li><a href="/delete/${judge.getId()}">delete profile</a></li>
        </ul>
    </div>
    <#if judge.getAffiliation()??>
        <h3 class="affiliation">(Affiliated with <span>${judge.getAffiliation()}</span>)</h3>
    </#if>
    <#if judge.getParadigmLink()??>
        <div class="row">
            <#if judge.getParadigmLink()?contains("judgephilosophies.wikispaces.com")>
                <div class="full column"><h2 class="paradigm-link"><a href="${judge.getParadigmLink()}">Paradigm on JudgePhilosophies</a></h2></div>
            </#if>
            <#if judge.getParadigmLink()?contains("https://www.tabroom.com/index/paradigm")>
                <div class="full column"><h2 class="paradigm-link"><a href="${judge.getParadigmLink()}">Paradigm on Tabroom</a></h2></div>
            </#if>
        </div>
    </#if>

    <div class="row">
        <div class="column one-half">
            <h2 class="center-text">Paradigm:</h2>
            <pre class="sql-output">${judge.getParadigm()}</pre>
        </div>
        <div class="column one-half">
            <h2 class="center-text">Past RFDs:</h2>
            <pre class="sql-output">${judge.getVotesOn()}</pre>
        </div>
    </div>
</div>

<#include "footer.ftl">
