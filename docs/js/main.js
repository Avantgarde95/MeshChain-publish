(function () {
  'use strict';

  function getElementList(name) {
    return document.getElementsByClassName(name);
  }

  function getElement(name) {
    return getElementList(name)[0];
  }

  function forEach(list, job) {
    var i;

    for (i = 0; i < list.length; i++) {
      job(list[i]);
    }
  }

  // Let each link to open in the new tab.
  forEach(getElementList('Link'), function (element) {
    element.target = '_blank';
    element.rel = 'noopener noreferrer';
  });

  // Attach 'light box' for each figure.
  forEach(getElementList('Section_FigureBox'), function (element) {
    new Luminous(element, {
      showCloseButton: true
    });
  });

  // Set attributes for YouTube videos.
  forEach(getElementList('Section_VideoContent'), function (element) {
    element.width = 560;
    element.height = 315;
    element.frameBorder = 0;
  });
}());
