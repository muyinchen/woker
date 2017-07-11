# Role Based Authentication using Vue.js 2

### Introduction

Front end development has progressed leaps and bounds as compared to the early days of development for clients. The developer community has contributed tremendously in creating frameworks and libraries like angular and react. With multiple choices at our disposal, consequently there are debate on which frameworks to choose. I developed a keen interest in Vue.js for its simple learning curve and readability of code. The key highlight is that the program functionality can be extended by adding plugins with very clear documentation provided in official website [https://vuejs.org/v2/guide/](https://vuejs.org/v2/guide/)

### What Is Role Based Authentication

> *Role-based authentication* is an approach to restricting system access to authorised users.

Consider you are creating an application for an apartment or a group housing facility, there are two user roles:

- Resident : Who performs actions like pay maintenance bills (example only) etc.
- Admin: Manages the entire set of resident accounts

Our agenda from client side is to hide or restrict certain elements for a particular user based on their specific role.

### Folder Structure

We have used Vue Cli for scaffolding, To install please visit [https://github.com/vuejs/vue-cli](https://github.com/vuejs/vue-cli)

Folder structure is subjective to each developer. I have followed the below mentioned type of structure for large applications. Under component folder create individual components.

Considering the above mentioned use case, there are three sub component folders ( admin component , login component , resident component ).

Each sub component will consist of all items like (.html file, .scss file, services file and .vue file). A separate router folder is created and all routes will be available under that folder.

Common components like header and footer are added as directives and not grouped as separate components. They are used in App.vue

### Creating And Implementing Logic

For the existing use case, create components for login, admin and resident & Create route file for the three components.

![img](https://cdn-images-1.medium.com/max/1600/1*P2kDS2w54Ywjrh9Lyve2sw.png)

### Check Authentication

The first step is to login , the user credential is captured and from the login.service.js the user credential is sent to the server. The server returns with success, along with **token** and **role**

This server response is sent to login.vue, where if the response is success then the return data is stored in local storage. If the response is a failure the user is redirected to the login page.

![img](https://cdn-images-1.medium.com/max/1600/1*gWhbbrYYyAOtw-jbdSxvKQ.png)

### Role Based Authentication

Once the user has cleared authentication, the next step is to identify the role and restrict the subsequent path based on user role. To perform this go to index.js under route folder.

Vue js provides route meta fields which can be added as an object with key as “meta”. You can check the documentation for how to add meta fields to route ( [https://router.vuejs.org/en/advanced/meta.html](https://router.vuejs.org/en/advanced/meta.html) )

![img](https://cdn-images-1.medium.com/max/1600/1*nvzpzESxalBRTbj-hXgATg.png)![img](https://cdn-images-1.medium.com/max/1000/1*nvzpzESxalBRTbj-hXgATg.png)

Once meta tag is added,

Use Navigation Guards ( [https://router.vuejs.org/en/advanced/navigation-guards.html](https://router.vuejs.org/en/advanced/navigation-guards.html) )to check if, the user is allowed to enter a particular page or not used on the user’s authentication and role. To perform this action use router.beforeeach which is a Global Guards (navigation guard component).

![img](https://cdn-images-1.medium.com/max/1600/1*lVXHLzv11Lv6VMS8dHzRBg.png)

Further the validation of the user authentication can also be done using router.beforeeach since, the return data is stored in local storage.

The next step is to create two more meta tag , one for AdminAuth and Resident Auth. Based on the meta tag and using router.beforeeach, we determine which user is allowed for a particular path based on the role.

For example: admin can only access “/admin”

![img](https://cdn-images-1.medium.com/max/1600/1*V8Myd3HqPYIy363so0oyqA.png)![img](https://cdn-images-1.medium.com/max/1000/1*V8Myd3HqPYIy363so0oyqA.png)

### Vuex State

Vuex is a **state management pattern + library** for Vue.js applications. It is required to know if the user is authenticated or not and this information has to be shared across all components. In the given use case the log out button should be visible only if the user is authenticated.

![img](https://cdn-images-1.medium.com/max/1600/1*hlVqp-E2eiiTxPNf0pNZXw.png)

### Download the source code

To have a better understanding, download the source code from GitHub and run the following command to launch the app.

[SourceCode](https://github.com/manojkumar3692/Vuejs-Authentication)