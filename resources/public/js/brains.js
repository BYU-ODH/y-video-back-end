  var BASIC_LIMIT = 144
  var ADVANCED_LIMIT = 300

  // Numpad functionality

  function numPress(numpad, number) {
    // console.log(numpad);
    // console.log(number);
    if (number != "-1") {
      addDigit(numpad, number);
    } else {
      removeDigit(numpad);
    }
  }

  function addDigit(factorField, digit) {
    factorID = "factor" + factorField;
    console.log(factorID);

    factorField = document.getElementById(factorID);
    contents = factorField.innerHTML;
    if (contents.length >= 3) {
      return
    }
    factorField.innerHTML = contents + digit

  }

  function removeDigit(factorField) {
    factorID = "factor" + factorField;
    console.log(factorID);

    factorField = document.getElementById(factorID);
    contents = factorField.innerHTML;
    if (contents.length > 0) {
      contents = contents.substring(0, contents.length - 1);
    }
    factorField.innerHTML = contents;

  }

  function stopTampering() {
    alert("hey");
  }

  // Tracking factoring functionality

  var guessedPairs
  var factorPairs
  var totalProblemsPassed
  var product

  function loadFirstProblem() {
    setupPage()
    totalProblemsPassed = 0
    loadNewProblem()
    if (mode == 0) {
      cookieTotalProblemsPassed = getCookie('totalProblemsPassedBasic')
    } else {
      cookieTotalProblemsPassed = getCookie('totalProblemsPassedAdvanced')
    }
    if (cookieTotalProblemsPassed != "") {
      totalProblemsPassed = parseInt(cookieTotalProblemsPassed)
      document.getElementById('totalProblemsPassed').innerHTML = totalProblemsPassed
    }
  }

  function loadNewProblem() {
    if (mode == 0) {
      // Basic mode
      if (getCookie("getNewBasic") == "false") {
        // Use the product stored in cookie
        console.log("getting basic cookie product")
        product = getCookie("basicProduct");
      } else {
        product = getNumber()
        setCookie("basicProduct", product, 160);
        setCookie("getNewBasic", "false", 160)
      }
    } else {
      // Advanced mode
      if (getCookie("getNewAdvanced") == "false") {
        // Use the product stored in cookie
        console.log("getting advanced cookie product")
        product = getCookie("advancedProduct");
      } else {
        product = getNumber()
        setCookie("advancedProduct", product, 160);
        setCookie("getNewAdvanced", "false", 160);
      }
    }
    resetForNewProblem()
    document.getElementById('numberToFactor').innerHTML = product
    factorPairs = findFactors(product)
    document.getElementById('possiblePairs').innerHTML = factorPairs.length
    setGuessedPairs()
  }

  function resetForNewProblem() {
    factorPairs = []
    guessedPairs = []
    document.getElementById('factor1').innerHTML = ""
    document.getElementById('factor2').innerHTML = ""
    // document.getElementById('grader').innerHTML = ""
    document.getElementById('possiblePairs').innerHTML = ""
    document.getElementById('guessedPairs').innerHTML = ""
    setSubmitButtons(true)
  }

  function findFactors(product) {
    // Returns array of L2 arrays of factors for product
    var factorPairs = []
    var i;
    for (i = 0; i < (product / 2) + 1; i++) {
      if ((product % i) == 0) {
        newPair = [i, product / i]
        reversedPair = [product / i, i]
        if (!isInArray(newPair, factorPairs)) {
          factorPairs.push([i, (product / i)])
        }
      }
    }
    return factorPairs
  }

  function isInArray(item, array) {
    // Checks if item (an array of length 2) is in array
    // console.log("in isInArray with:")
    // console.log("item", item, "array", array)
    for (j = 0; j < array.length; j++) {
      if (item[0] == array[j][0] && item[1] == array[j][1] ||
          item[0] == array[j][1] && item[1] == array[j][0]) {
        // console.log(item, "is in", array)
        return true
      }
    }
    return false
  }

  function clicked() {
    submitAnswers()
  }

  function getNumber() {
    if (mode == 0) {
      baseFactorOne = (getRandomInt(11) + 1)
      baseFactorTwo = (getRandomInt(11) + 1)
      return baseFactorOne * baseFactorTwo
    } else {
      while ((toReturn = getRandomInt(ADVANCED_LIMIT)) == 0) {
        // Loop until non-zero number is found
      }
      return toReturn
    }
  }

  function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
  }

  function submitButtonPress() {
    if (guessedPairs.length == factorPairs.length) {
      nextProblem()
    } else {
      submitAnswers()
    }
  }

  function setSubmitButtons(submit) {
    allButtons = document.getElementsByClassName('submitButton')
    if (submit) {
      for (var i = 0; i < allButtons.length; i++) {
        allButtons[i].innerHTML = "Submit"
        allButtons[i].className = "btn btn-success numpadbutton submitButton"
      }
    } else {
      for (var i = 0; i < allButtons.length; i++) {
        allButtons[i].innerHTML = "Next Problem"
        allButtons[i].className = "btn btn-warning numpadbutton submitButton"
      }
    }
  }

  function submitAnswers() {
    console.log("in submitAnswers")
    // product = parseInt(document.getElementById('numberToFactor').innerHTML)
    factor1 = parseInt(document.getElementById('factor1').innerHTML)
    factor2 = parseInt(document.getElementById('factor2').innerHTML)

    document.getElementById('factor1').innerHTML = ""
    document.getElementById('factor2').innerHTML = ""

    console.log("checking for previous guess")
    if (isInArray([factor1, factor2], guessedPairs)) {
      // document.getElementById('grader').innerHTML = "You already guessed that pair"
      return
    }

    console.log("checking factors")
    if (checkFactors(product, factor1, factor2)) {
      // Factors 1 and 2 do multiply to Product
      // document.getElementById('grader').innerHTML = "correct!"
      // console.log(typeof factor1)
      if (factor1 < factor2) {
        guessedPairs.push([factor1, factor2])
        // console.log(factor1, "<", factor2)
      } else {
        guessedPairs.push([factor2, factor1])
        // console.log(factor2, "<", factor1)
      }
      setGuessedPairs()
      setPairsRemaining()
    } else {
      // Incorrect factors
      // document.getElementById('grader').innerHTML = "try again!"
    }

    if (guessedPairs.length == factorPairs.length) {
      setTotalProblemsPassed(parseInt(totalProblemsPassed) + 1)
      if (mode == 0) {
        // Basic mode
        setCookie("getNewBasic", "true", 160);
        setCookie("totalProblemsPassedBasic", totalProblemsPassed, 160)
      } else {
        // Advanced mode
        setCookie("getNewAdvanced", "true", 160);
        setCookie("totalProblemsPassedAdvanced", totalProblemsPassed, 160)
      }
      setSubmitButtons(false)
    }

  }

  function setTotalProblemsPassed(newAmount) {
    totalProblemsPassed = newAmount
    document.getElementById('totalProblemsPassed').innerHTML = totalProblemsPassed
  }

  function setGuessedPairs() {
    console.log("in setGuessedPairs")
    var pairsString = ""
    // product = document.getElementById('numberToFactor').innerHTML;
    // Sort guessedPairs by first elements
    console.log("sorting guessPairs")
    guessedPairs.sort(function(a,b){return a[0] > b[0];});
    console.log("populating list")
    for (i = 0; i < factorPairs.length; i++) {
      var classes = ""
      console.log("checking membership")
      if (guessedPairs.length > 0 && isInArray(factorPairs[i], guessedPairs)) {
        // Pair has been guessed
        console.log("pair has been guessed")
        outString = factorPairs[i][0] + " * " + factorPairs[i][1] + " = " + product
        classes = "guessed"
      } else {
        // Pair has not been guessed
        console.log("pair has not been guessed")
        outString = "_ * _ = " + product
      }

      pairsString += "<h2 class=\""
      pairsString += classes
      pairsString += "\">"
      pairsString += outString
      pairsString += "</h2>"
    }
    document.getElementById('guessedPairs').innerHTML = pairsString
  }

  function setPairsRemaining() {
    console.log("in setPairsRemaining")
    document.getElementById("possiblePairs").innerHTML = (factorPairs.length - guessedPairs.length)
  }

  function checkFactors(product, factor1, factor2) {
    console.log("in checkFactors")
    if ((product / factor1) == factor2) {
      return true
    } else {
      return false
    }
  }

  function nextProblem() {
    if (guessedPairs.length == factorPairs.length) {
      loadNewProblem()
    }
  }

  function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
  }

  function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    console.log(ca)
    for(var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
  }

  // Mode functionality

  // 0 = basic, 1 = advanced
  var mode = 0;

  function setupPage() {
    console.log("in setupPage");
    mode = getCookie("mode");
    console.log("mode: ", mode);
    setModeButton();
  }

  function toggleMode() {
    console.log("in toggle mode")
    if (mode == 0) {
      // Basic mode -> Advanced mode
      mode = 1;
      setCookie("totalProblemsPassedBasic", totalProblemsPassed, 160)
      newTotalProblemsPassed = getCookie("totalProblemsPassedAdvanced")
    } else {
      // Advanced mode -> Basic mode
      mode = 0;
      setCookie("totalProblemsPassedAdvanced", totalProblemsPassed, 160)
      newTotalProblemsPassed = getCookie("totalProblemsPassedBasic")
    }
    if (newTotalProblemsPassed == "") {
      newTotalProblemsPassed = 0
    }
    setTotalProblemsPassed(newTotalProblemsPassed)
    setCookie("mode", mode, 160);
    setModeButton();
    loadNewProblem();
  }

  function setModeButton() {
    modeButton = document.getElementById('modeButton')
    if (mode == 0) {
      // Basic mode
      modeButton.className = "btn btn-warning"
      modeButton.innerHTML = "Basic Mode"
    } else {
      // Advanced mode
      modeButton.className = "btn btn-danger"
      modeButton.innerHTML = "Advanced Mode"
    }
  }

  // Development functionality

  function manualGetProduct() {
    console.log(getNumber())
  }
