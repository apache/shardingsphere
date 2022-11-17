$(document).ready(function () {
  // roadmap section
  $('.roadmapSlick').slick({
    infinite: false,
    speed: 300,
    variableWidth: true,
    draggable: false,
    arrows: true,

    responsive: [
      {
        breakpoint: 868,
        settings: {
          centerMode: true,
          slidesToShow: 1,
          slidesToScroll: 1,
          draggable: true,
          arrows: false,
        },
      },
    ],
  })

  $('.roadmapSlick').on(
    'beforeChange',
    function (event, slick, currentSlide, nextSlide) {
      if (nextSlide > 0) {
        $('.shadow.left').show()
      } else {
        $('.shadow.left').hide()
      }

      let rightEndIndex = 6

      // get window width
      let windowWidth = $(window).width()

      if (windowWidth > 1900) {
        rightEndIndex = 0
      } else if (windowWidth > 1380) {
        rightEndIndex = 1
      } else if (windowWidth > 1124) {
        rightEndIndex = 2
      } else if (windowWidth > 868) {
        rightEndIndex = 3
      } else if (windowWidth > 820) {
        rightEndIndex = 4
      } else if (windowWidth > 740) {
        rightEndIndex = 5
      }
      console.log(nextSlide)
      if (nextSlide > rightEndIndex) {
        $('.slick-next.slick-arrow').hide()
        $('.shadow.right').hide()
      } else {
        $('.slick-next.slick-arrow').show()
        $('.shadow.right').show()
      }
    }
  )

  $('.shadow.left').hide()

  // navigation
  $('.navigation .iconMenu').click(function () {
    $('.mobileMenu').show()
    bodyScrollLock.disableBodyScroll(document.querySelector('.mobileMenu'))
  })

  $('.languageSelect + .langs').hide()
  $('.mobileMenu .itemList').hide()

  $('.mobileMenu .item').click(function () {
    // this item
    let item = $(this)
    item.find('.arrowIcon').toggleClass('active')
    item.find('+ .itemList').slideToggle()
  })

  $('.mobileMenu .x').click(function () {
    $('.mobileMenu').hide()
    bodyScrollLock.enableBodyScroll(document.querySelector('.mobileMenu'))
  })

  // footer
  function checkFooterSize() {
    let windowWidth = $(window).width()

    if (windowWidth < 740) {
      $('.footerNavigation .links').hide()
    } else {
      $('.footerNavigation .links').show()
    }
  }

  checkFooterSize()

  // window resize hide links
  $(window).resize(function () {
    checkFooterSize()
  })

  $('.footerNavigation .item').click(function () {
    let windowWidth = $(window).width()

    if (windowWidth >= 740) {
      return
    }

    // this item
    let item = $(this)
    item.find('.links').slideToggle()
  })
})
