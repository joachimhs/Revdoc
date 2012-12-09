Ember.TEMPLATES['application'] = Ember.Handlebars.compile('' +
    '{{outlet}}'
);

Ember.TEMPLATES['login-page'] = Ember.Handlebars.compile('' +
    '<div class="loginBox well">' +
        '<h1>BookReview Login</h1> ' +
        '<button {{action doLogin}} class="tenPxMarginTop">Login With Mozilla Persona</button>' +
    '</div>'
);

Ember.TEMPLATES['documents-page'] = Ember.Handlebars.compile('' +
    '<h1>Documents</h1>' +
    '<div>{{view Ember.TextField valueBinding="newDocumentTitle"}}<button {{action newDocument}}>New Document</button></div>' +

    '{{#each controller}}' +
        '<div ><a {{action selectDocument this href=true}}>{{title}}</a></div>' +
    '{{/each}}'
);

Ember.TEMPLATES['chapters-page'] = Ember.Handlebars.compile('' +
    '{{#each controller}}' +
        '<div ><a {{action selectChapter this href=true}}>{{title}}</a></div>' +
    '{{/each}}'
);

Ember.TEMPLATES['show-documents-page'] = Ember.Handlebars.compile('' +
    '{{#if content}}' +
        '<h1>{{title}}</h1>' +
        '<div>{{view Ember.TextField valueBinding="newChapterTitle"}}<button {{action newChapter}}>New Chapter</button></div>' +
        '{{#each chapter in content.chapters}}' +
            '<div ><a {{action selectChapter chapter href=true}}>{{chapter.title}}</a></div>' +
        '{{/each}}' +
    '{{/if}}'
);

Ember.TEMPLATES['chapter-page'] = Ember.Handlebars.compile('' +
    '{{#if title}}<div class="chapterHeadline">' +
        '<h1>{{index}}</h1>' +
        '{{title}}' +
    '</div>{{/if}}' +
    '<div class="rightMargin">' +
        '{{view BookReview.FileUploadView elementId="attachFile"}}' +
    '</div>' +
    '{{#if abstract.isLoaded}}' +
        '<div class="chapterAbstract">' +
            '<ul>' +
                '{{#each paragraph in abstract.itemList}}' +
                    '<li {{bindAttr id="paragraph.id"}}>{{paragraph.content}}</li>' +
                '{{/each}}' +
            '</ul>' +
            '{{#each paragraph in abstract.paragraphs}}' +
                '{{view Ember.View paragraphBinding="paragraph" templateName="paragraph-template"}}' +
            '{{/each}}' +
        '</div>' +
    '{{/if}}' +
    '{{#if sections.isLoaded}}' +
    //'{{view Ember.View controllerBinding="sortedSections" templateName="sections-template"}}' +
    '{{#each section in BookReview.router.chapterSectionsController.arrangedContent}}' +
        '<div class="rightMargin">' +
            '<a {{action addSection item}}>Add Section</a>' +
        '</div>' +
        '{{view Ember.View sectionBinding="item" templateName="section-template"}}' +
        '{{#each subItem in item.subSections}}' +
            '{{view Ember.View sectionBinding="subItem" templateName="section-template"}}' +
        '{{/each}}' +
    '{{/each}}' +
        /*'{{#each item in sortedSections.arrangedContent}}' +

        '{{/each}}' +*/
    '{{/if}}'
);

Ember.TEMPLATES['paragraphs-template'] = Ember.Handlebars.compile('' +
        '<div class="rightMargin">' +
            '<a {{action editParagraph paragraph}}>Edit paragraph</a>' +
            '<a {{action addParagraph paragraph}}>Add paragraph</a>' +
        '</div>' +
        '{{#if paragraph.isEditing}}' +
            '{{view Ember.TextArea valueBinding="paragraph.content" classNames="paragraphTextArea"}} ' +
            '<br /> ' +
            '<button {{action saveParagraph paragraph}}>Save Paragraph</button>' +
        '{{else}}' +
            '{{view Ember.View paragraphBinding="paragraph" templateName="paragraph-template"}}' +
        '{{/if}}'
);
Ember.TEMPLATES['paragraph-template'] = Ember.Handlebars.compile('' +
    '<div class="leftMargin">' +
        '(<a {{action toggleComments paragraph href=false}}>{{paragraph.numComments}} comments)</a>' +
    '</div>' +
    '<p {{bindAttr id="paragraph.id"}}>' +
        '{{#if paragraph.isList}}' +
            '{{paragraph.listContent}}' +
        '{{else}}' +
            '{{paragraph.content}}' +
        '{{/if}}' +
        '{{#if paragraph.figure.isLoaded}}' +
            '<img {{bindAttr src="paragraph.figure.link"}} class="figure" /><br />Figure {{paragraph.figure.title}}' +
        '{{/if}}' +
        '{{#if paragraph.example.isLoaded}}' +
            'Example {{paragraph.example.title}}<br />' +
            '<pre>' +
                '{{paragraph.example.content}}' +
            '</pre>' +
        '{{/if}}' +
        '{{#if paragraph.showComments}}' +
            '<div class="showComments">' +
                '{{view Ember.TextArea valueBinding="paragraph.newComment" classNames="paragraphTextArea"}}<br />' +
                '<button {{action addComment paragraph}}>Add Comment</button>' +
                '{{#each comment in paragraph.comments}}' +
                    '<div class="comment">{{comment.commentFrom}}: {{comment.commentDate}} - {{comment.commentContent}}</div>' +
                '{{/each}}' +
            '</div>' +
        '{{/if}}' +
    '</p>'
);

Ember.TEMPLATES['section-template'] = Ember.Handlebars.compile('' +
    '<div class="chapterSection">' +
        '{{#if view.section.hasHeader}}' +
            '<h1>{{view.section.chapter.index}}.{{view.section.sectionIndex}} {{view.section.title}}</h1>' +
        '{{/if}}' +
        '{{#with view.section}}' +
            '{{#each paragraph in sortedParagraphs.arrangedContent}}' +
                '{{view Ember.View paragraphBinding="paragraph" templateName="paragraphs-template"}}' +
            '{{/each}}' +
        '{{/with}}' +
    '</div>'
);