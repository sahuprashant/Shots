# Shots
## This app uses Firebase Cloud Functions in Nodejs format to send an email using email services of 'gmail'
Any Google user can use Firebase Cloud functions, use its services and deploy the functions on Firebase server. These functions can be used for any platform e.g. Website,iOS,Android,etc. Using these functions we can reduce the computational load on local app and use the functions instead to do our work. Following steps I followed to use the Firebase functions in my app and was able to send an email: 
- Install Nodejs in your pc and install Firebase CLI using npm
- After installing Firebase CLI. Log In in the command window and it will prompt a google login page in browser
- After loggin in now I can deploy my functions on Firebase server using command `firebase deploy`
- To write script in Nodejs I took guidance from [Cloud Functions for Firebase](https://www.youtube.com/watch?v=DYfP-UIKxH0&index=1&list=PLl-K7zZEsYLkPZHe41m4jfAxUi0JjLgSM) and this [repository](https://github.com/firebase/functions-samples/tree/f4edd3390dda80e7da2a27ec51bedcc0a4fa7d92/quickstarts/email-users)
- After deploying this function I made the HTTP request for this function from my app and it triggered my function on Firebase server and promped it to send the email
- To able to send email using 'gmail' service you have to allow your google account to allow access to [less secure app](https://www.google.com/settings/security/lesssecureapps)
- After deploying the function and triggering it now you can see the logs in console of Firebase under Functions section

Apart from sending email there is a lot of scope of Firebase Cloud Functions.
