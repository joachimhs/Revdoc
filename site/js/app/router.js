    BookReview.router = Ember.Router.create({
        enableLogging: true,
        //location: 'history',
        root: Ember.Route.extend({
            loginSuccessful: function() {
                console.log('loginSuccessful');
            },

            doLogOut: Ember.Route.transitionTo('login'),

            index: Ember.Route.extend({
                route: '/',
                redirectsTo: 'login'
            }),
            login: Ember.Route.extend({
                route: '/login',
                doLogin: function(router) {
                    navigator.id.request();
                },

                loginSuccessful: function() {
                    console.log('loginSuccessful');
                    BookReview.router.transitionTo('documents.index');
                },

                connectOutlets: function (router) {
                    router.get('applicationController').connectOutlet('login');
                }
            }),
            documents: Ember.Route.extend({
                route: '/documents',
                initialState: 'index',

                selectDocument: Ember.Route.transitionTo('showDocument.index'),

                index: Ember.Route.extend({
                    route: '/',

                    newDocument: function(router, event) {
                        var document = BookReview.Document.createRecord({
                            id: Math.uuid(16, 16),
                            title: BookReview.router.get('documentsController.newDocumentTitle'),
                            chapters: []
                        });

                        BookReview.store.commit();
                        BookReview.router.get('documentsController').set('newDocumentTitle', '');
                    },

                    connectOutlets: function(router) {
                        router.get('applicationController').connectOutlet('documents', BookReview.store.findAll(BookReview.Document));
                    }
                }),

                showDocument: Ember.Route.extend({
                    route: '/document',
                    initialState: 'index',
                    selectChapter: Ember.Route.transitionTo('chapter'),

                    newChapter: function(router, event) {
                        BookReview.store.commit();

                        var chapter = BookReview.Chapter.createRecord({
                            id: Math.uuid(16, 16),
                            document: BookReview.router.get('showDocumentController.content'),
                            title: BookReview.router.get('showDocumentController.newChapterTitle'),
                            index: BookReview.router.get('showDocumentController').getNextIndex(),
                            abstract: null,
                            sections: []
                        });

                        BookReview.store.commit();
                        BookReview.router.get('showDocumentController').set('newChapterTitle', '');
                    },

                    index: Ember.Route.extend({
                        route: '/:document_id',
                        initialState: 'showDocumentChapters',

                        connectOutlets: function(router, document) {
                            router.get('showDocumentController').set('content',document);
                        },

                        showDocumentChapters: Ember.Route.extend({
                            route: '/',

                            connectOutlets: function(router) {
                                router.get('applicationController').connectOutlet('showDocument');//, BookReview.Document.find(document.get('id')));
                            }
                        }),

                        chapter: Ember.Route.extend({
                            route: "/:chapter_id",

                            toggleComments: function(router, event) {
                                var paragraph = event.context;
                                if (paragraph) {
                                    paragraph.set('showComments', !paragraph.get('showComments'));
                                }
                            },

                            addComment: function(router, event) {
                                console.log(event.context);
                                console.log(event.context.get('newComment'));
                                console.log(event.context.get('chapterSection.chapter'));

                                var comment = BookReview.ChapterParagraphComment.createRecord({
                                    id: Math.uuid(16,16),
                                    documentId: BookReview.router.get('showDocumentController.content.id'),
                                    chapterId: event.context.get('chapterSection.chapter.id'),
                                    chapterParagraph: event.context.get('id'),
                                    commentContent: event.context.get('newComment'),
                                    commentDate: new Date(),
                                    commentFrom: BookReview.get('uuidToken')
                                });

                                event.context.get('comments').pushObject(comment);
                                event.context.set('newComment', '');

                                BookReview.store.commit();
                            },

                            addSection: function(router, event) {
                                console.log(event.context);
                                var section = BookReview.ChapterSection.createRecord({
                                    id: Math.uuid(16, 16),
                                    chapter: BookReview.router.get('chapterController.content'),
                                    title: 'New Section',
                                    paragraphs: [],
                                    subSections: []
                                });
                            },

                            editParagraph: function(router, event) {
                                console.log('editing Paragraph: ' + event.context.get('id'));
                                event.context.set('isEditing', true);
                            },

                            saveParagraph: function( router, event) {
                                console.log('saving Paragraph: ' + event.context.get('id'));
                                event.context.set('isEditing', false);
                            },

                            addParagraph: function(router, event) {
                                var chapterSection = event.context.get('chapterSection');
                                var currIndex = event.context.get('paragraphIndex');

                                chapterSection.addParagraphAtIndex(currIndex);

                                //BookReview.router.chapterController.sortChapterContents();
                            },

                            connectOutlets: function(router, chapter) {
                                router.get('chapterSectionsController').connectControllers('chapter');
                                router.get('applicationController').connectOutlet('chapter', chapter);
                            }
                        })
                    })


                    /*index: Ember.Route.extend({
                        route: "/",

                        selectChapter: Ember.Route.transitionTo('chapter'),

                        connectOutlets: function(router, document) {
                            router.get('applicationController').connectOutlet('showDocument');
                        },

                        chapter: Ember.Route.extend({
                            route: "/chapter/:chapter_id",

                            connectOutlets: function(router, chapter) {
                                router.get('applicationController').connectOutlet('chapter', BookReview.Chapter.find(chapter.get('id')));
                            }
                        })
                    })*/
                })
            }),
            chapters: Ember.Route.extend({
                route: '/chapters',

                selectChapter: Ember.Route.transitionTo('chapter'),

                connectOutlets: function (router) {
                    router.get('applicationController').connectOutlet('chapters', BookReview.store.findAll(BookReview.Chapter));

                    /*EurekaJ.store.findAll(EurekaJ.MainMenuModel);
                    var mainMenu = EurekaJ.store.filter(EurekaJ.MainMenuModel, function(data) {
                        if (data.get('parentPath') === null) { return true; }
                    });

                    router.get('applicationController').connectOutlet('main');
                    router.get('menuController').set('content', mainMenu);
                    router.get('applicationController').connectOutlet('header', 'header');
                    */
                },
                exit: function() {
                    BookReview.log('exit Home');
                }
            })
        })
    });