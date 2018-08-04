const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
var db = admin.database();
const nodemailer = require('nodemailer');

const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;
const mailTransport = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});
const APP_NAME = 'Shots';

exports.sendanEmail = functions.https.onCall((data,context) => {
	const cuid = data.uid;
	const displayName = context.auth.token.name || null;
	const picture = context.auth.token.picture || null;
	const email = context.auth.token.email || null;
	console.log('Write to be message: '+cuid);
	return sendLoginEmail(email, displayName,picture)
	.then(() => {
		console.log('New message written!');
	})
	.catch((error) => {
		throw new functions.https.HttpsError('unknown pras',error.message,error);
	});
});

function sendLoginEmail(email, displayName,picture) {
	const mailOptions = {
	  from: `${APP_NAME} <noreply@firebase.com>`,
	  to: 'psahu274@gmail.com',
	};
  
	mailOptions.subject = `Info from ${APP_NAME}!`;
	mailOptions.text = `Hey, ${displayName} just logged In. Email: ${email} and Photo: ${picture}`;
	return mailTransport.sendMail(mailOptions).then(() => {
	  return console.log('Logged in mail sent to:', email);
	});
}