const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
var db = admin.database();
const nodemailer = require('nodemailer');

const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;
const mailTransport = nodemailer.createTransport({
  service: 'Gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});
var APP_NAME = 'Shots';

exports.firstMessage = functions.https.onCall((data,context) => {
	const cuid = data.uid;
	var displayName = context.auth.token.name || null;
	var picture = context.auth.token.picture || null;
	var email = context.auth.token.email || null;
	console.log('Write to be message: '+gmailEmail);
	return sendLoginEmail(email, displayName,picture)
	.then(() => {
		console.log('Mail sent successfully!');
		return null;
	})
	.catch((error) => {
		throw new functions.https.HttpsError('unknown pras',error.message,error);
	});
});

function sendLoginEmail(email, displayName,picture) {
	const mailOptions = {
	  from: `${APP_NAME} <noreply@firebase.com>`,
	  to: 'ch13b1016@iith.ac.in',
	};
	
	mailOptions.subject = `Info from ${APP_NAME}!`;
	mailOptions.text = `Hey, ${displayName} just logged In. Email: ${email} and Photo: ${picture}`;
	console.log('sendloginemail'+mailOptions.text);
	return mailTransport.sendMail(mailOptions).then(() => {
	  return console.log('Logged in mail sent to:', email);
	});
}