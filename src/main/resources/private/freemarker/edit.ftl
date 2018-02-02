<#include "header.ftl">


<div id="container">
    <h2>Edit ${judge.getLastname()}'s Profile...</h2>
    <form id="form-root" method="post" action="/update-profile">
        <input type="hidden" name="id" value="${judge.getId()}">
        <div>
            <label for="first">Judge's First Name</label>
            <input name="first" id="first" type="text" value="${judge.getFirstname()}">
        </div>
        <div>
            <label for="last">Judge's Last Name</label>
            <input name="last" id="last" type="text" value="${judge.getLastname()}" required>
        </div>
        <div>
            <label for="affiliation">Judge's School Affiliation *</label>
            <#if judge.getAffiliation()??>
                <input name="affiliation" id="affiliation" type="text" value="${judge.getAffiliation()}">
            <#else>
                <input name="affiliation" id="affiliation" type="text">
            </#if>
        </div>
        <div>
            <label for="speed">Comfort with Speed (1-5)</label>
            <input name="speed" id="speed" type="number" min="1" max="5" value="${judge.getSpeed()}" required>
        </div>
        <div>
            <label for="flow">Judge's Amount of Flowing (1-5)</label>
            <input name="flow" id="flow" type="number" min="1" max="5" value="${judge.getFlowing()}" required>
        </div>
        <div>
            <label for="paradigm-link">Judge Paradigm on <a href="https://judgephilosophies.wikispaces.com">JudgePhilosophies</a> or <a href="https://www.tabroom.com/index/paradigm.mhtml">Tabroom</a> *</label>
            <input name="paradigm-link" id="paradigm-link" type="text" value="${(judge.getParadigmLink())!""}">
        </div>
        <div>
            <label for="paradigm">What was your judge's self-described paradigm?</label>
            <br>
            <textarea rows="8" cols="45" id="paradigm" name="paradigm" required>${judge.getParadigm()}</textarea>
        </div>
        <div>
            <label for="voteson">What did the judge vote on?</label>
            <br>
            <textarea rows="4" cols="25" id="voteson" name="voteson" required>${judge.getVotesOn()}</textarea>
        </div>
        <button class="submit" >Submit</button>
    </form>
    <p>* means an optional field</p>
</div>

<#include "footer.ftl">