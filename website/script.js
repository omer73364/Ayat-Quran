const menu = document.querySelector('.menu');
const navigation = document.querySelector('#navigation');
function closeMenu() {
  navigation.classList.remove('open');
  menu.setAttribute('aria-expanded', 'false');
  menu.setAttribute('aria-label', 'فتح القائمة');
}
menu.addEventListener('click', () => {
  const open = navigation.classList.toggle('open');
  menu.setAttribute('aria-expanded', String(open));
  menu.setAttribute('aria-label', open ? 'إغلاق القائمة' : 'فتح القائمة');
});
navigation.querySelectorAll('a').forEach(link => link.addEventListener('click', closeMenu));
document.addEventListener('keydown', event => { if (event.key === 'Escape') closeMenu(); });
const screenDescriptions = {reader: 'شاشة القراءة بملء الشاشة', saved: 'المحفوظات والبحث في الآيات', reminders: 'حالة التذكيرات وإعداداتها', home: 'الرئيسية وتقدم الورد اليومي'};
const tabs = [...document.querySelectorAll('[role="tab"]')];
const screenImage = document.querySelector('#screen-image');
let imageTimer;
function selectScreen(tab, focus = false) {
  tabs.forEach(item => {
    item.setAttribute('aria-selected', String(item === tab));
    item.tabIndex = item === tab ? 0 : -1;
  });
  const screen = tab.dataset.screen;
  document.querySelector('#screen-panel').setAttribute('aria-labelledby', tab.id);
  document.querySelector('#screen-number').textContent = String(tabs.indexOf(tab) + 1).padStart(2, '0');
  clearTimeout(imageTimer);
  screenImage.classList.add('changing');
  imageTimer = setTimeout(() => {
    screenImage.src = `assets/${screen}.png`;
    screenImage.alt = screenDescriptions[screen];
    screenImage.classList.remove('changing');
  }, 120);
  if (focus) tab.focus();
}
tabs.forEach((tab, index) => {
  tab.addEventListener('click', () => selectScreen(tab));
  tab.addEventListener('keydown', event => {
    const movement = {ArrowDown: 1, ArrowUp: -1};
    let next;
    if (event.key in movement) next = (index + movement[event.key] + tabs.length) % tabs.length;
    else if (event.key === 'Home') next = 0;
    else if (event.key === 'End') next = tabs.length - 1;
    else return;
    event.preventDefault();
    selectScreen(tabs[next], true);
  });
});
