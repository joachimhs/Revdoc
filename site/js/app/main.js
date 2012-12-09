BookReview.initialize(BookReview.router);

// Mozilla Persona
navigator.id.watch({
    loggedInUser: null,
    onlogin: function(assertion) {
        $.ajax({
            type: 'POST',
            url: '/auth/login',
            data: {assertion: assertion},
            success: function(res, status, xhr) {
                if (res.authFailed) {
                    alert('Authentication Failed');
                } else if (res.uuidToken) {
                    BookReview.set('uuidToken', res.uuidToken);
                    document.cookie="uuidToken=" + res.uuidToken;
                    BookReview.router.send("loginSuccessful");
                }
            },
            error: function(xhr, status, err) { BookReview.router.send("doLogOut"); }
        });
    },

    onlogout: function() {
        $.ajax({
            type: 'POST',
            url: '/auth/logout',
            success: function(res, status, xhr) { console.log('onlogout: '); console.log(res); },
            error: function(xhr, status, err) { BookReview.router.send("doLogOut"); }
        });
    }
});