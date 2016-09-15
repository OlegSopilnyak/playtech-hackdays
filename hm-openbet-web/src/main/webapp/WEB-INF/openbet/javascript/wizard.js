/* Copyright (c) Adnan Jaswal, 2015. See the file license.txt for copying permission. */
/* Module for Wizard component */
var Wizard = function (steps, message) {
	/* add members here */
	/* number Of steps in the wizard */
	var numberOfSteps;
	/* current step the wizard is on */
	var currentStep = ko.observable();
	/* call back on wizard done */
	var doneCallBack;
	/* flag to show done message */
	var showDoneMessage = ko.observable(false);
	/* message for when the wizard is done */
	var doneMessage = ko.observable(message);
	
	/* method to go back a step */
	var back = function () {
		currentStep(currentStep() - 1);
	};
	
	/* method to go forward a step */
	var next = function () {
		currentStep(currentStep() + 1);
		showDoneMessage(false);
	};
	
	/* method for wizard done */
	var done = function () {
		//Done reset wizard and call transfer
		console.log("User clicked done.....");
		currentStep(1);
		doneCallBack();	
		showDoneMessage(true);
	};
	
	/* method sets the call back */
	var setCallBack = function (callBack) {
		doneCallBack = callBack;
	};
	
	/* returns true if the wizard is on the last step, false otherwise */
	var isLastStep = ko.pureComputed(function() {
	    return currentStep() == numberOfSteps;
	});

	/* returns true if the wizard is on the first step, false otherwise */
	var isFirstStep = ko.pureComputed(function() {
	    return currentStep() == 1;
	});
	
	/* method to initialise the module */
	var init = function () {
		numberOfSteps = steps;
		currentStep(1);
	};
	
	/* execute the init function */
	init();
	
	return {
		/* add members that will be exposed publicly */
		back: back,
		next: next,
		done: done,
		currentStep: currentStep,
		isLastStep: isLastStep,
		isFirstStep: isFirstStep,
		setCallBack: setCallBack,
		doneMessage: doneMessage,
		showDoneMessage: showDoneMessage
	};
};