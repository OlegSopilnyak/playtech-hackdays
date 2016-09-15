/* Copyright (c) Adnan Jaswal, 2015. See the file license.txt for copying permission. */
/* Module for server stub */
var ServerStub = function () {
	
	/* months for date string */
    var monthNames = [
                      "January", "February", "March",
                      "April", "May", "June", "July",
                      "August", "September", "October",
                      "November", "December"
                  ];
    
	/* add members here */
	var memberData = {
			personal: {		firstName: "John", lastName: "Citizen", 
							address: { 	street: "1 Main Street", city: "Melbourne", postCode: 3000, country: "Australia" }, 
							phoneNumber: 0399998888, emailAddress: "john.citizen@email.com"},
			accounts: [
			           {	summary: {branch: "Collingwood", number: "0612 30042", type: "Savings", balance: 700}, 
							transactions: [{date: "20 May", description: "Collingwood milk bar", category: "Grocery", amount: 23},
							               {date: "18 May", description: "Food store", category: "Food", amount: 13},
							               {date: "15 April", description: "Collingwood milk bar", category: "Grocery", amount: 53},
							               {date: "12 March", description: "Sushi shop", category: "Food", amount: 28}]
			           },
			           {	summary: {branch: "Clayton", number: "0652 20172", type: "Savings", balance: 313.64}, 
							transactions: [{date: "21 May", description: "Clayton milk bar", category: "Grocery", amount: 63},
							               {date: "19 May", description: "No 8 Southbank", category: "Food", amount: 450},
							               {date: "13 April", description: "State library", category: "Work", amount: 53},
							               {date: "11 March", description: "Bags for sale", category: "Shopping", amount: 78}]
			           },			
			           {	summary: {branch: "Mitcham", number: "0682 40742", type: "Credit", balance: 60000}, 
							transactions: [{date: "23 June", description: "Black coder coffee", category: "Food", amount: 53},
							               {date: "21 May", description: "Food store", category: "Grocery", amount: 63},
							               {date: "13 April", description: "Money transfer", category: "Transfer", amount: 500},
							               {date: "09 March", description: "Sushi shop", category: "Food", amount: 68},
							               {date: "08 March", description: "Donuts", category: "Food", amount: 50}]
			           },
			           {	summary: {branch: "Doncaster", number: "0612 40772", type: "Cheque", balance: 10000}, 
							transactions: []
			           }			           
			          ],
	};
	
	/* method returns member data */
	var getMemberData = function () {
		return memberData;
	};
	
	var getAccounts = function () {
		return memberData.accounts;
	};
	
	var getTodaysDate = function () {
		var date = new Date();
		var day = date.getDate();
		if( day < 10) {
			day = "0" + day
		}
	    var monthIndex = date.getMonth();
	    return day + ' ' + monthNames[monthIndex];
	};
	
	var transferFunds = function (transferModel) {
		//convert string to number
		transferModel.amount = + transferModel.amount;

		memberData.accounts.forEach(function(account) {
			if(account.summary.number == transferModel.toAccount.summary.number) {
				account.summary.balance = account.summary.balance + transferModel.amount;
				account.transactions.push({date: getTodaysDate(), description: transferModel.description, category: "Credit", amount: transferModel.amount});
			}

			if(account.summary.number == transferModel.fromAccount.summary.number) {
				account.summary.balance = account.summary.balance - transferModel.amount;
				account.transactions.push({date: getTodaysDate(), description: transferModel.description, category: "Debit", amount: transferModel.amount});
			}
		});
	};
	
	/* method to update personal information */
	var updatePersonalInformation = function (personalInformation) {
		memberData.personal.firstName = personalInformation.firstName;
		memberData.personal.lastName = personalInformation.lastName;
		memberData.personal.phoneNumber = personalInformation.contactDetails.phoneNumber;
		memberData.personal.emailAddress = personalInformation.contactDetails.emailAddress;
		
		memberData.personal.address.street = personalInformation.address.street;
		memberData.personal.address.city = personalInformation.address.city;
		memberData.personal.address.postCode = personalInformation.address.postCode;
		memberData.personal.address.country = personalInformation.address.country;
	};
	
	return {
		/* add members that will be exposed publicly */
		getMemberData: getMemberData,
		updatePersonalInformation: updatePersonalInformation,
		transferFunds: transferFunds,
		getAccounts: getAccounts

	};
};