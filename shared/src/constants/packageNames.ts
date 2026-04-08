export const SUPPORTED_MESSENGERS = {
  KAKAOTALK: 'com.kakao.talk',
  LINE: 'jp.naver.line.android',
  FACEBOOK_MESSENGER: 'com.facebook.orca',
  TELEGRAM: 'org.telegram.messenger',
  INSTAGRAM: 'com.instagram.android',
} as const;

export type MessengerPackageName = typeof SUPPORTED_MESSENGERS[keyof typeof SUPPORTED_MESSENGERS];

export const MESSENGER_DISPLAY_NAMES: Record<MessengerPackageName, string> = {
  [SUPPORTED_MESSENGERS.KAKAOTALK]: 'KakaoTalk',
  [SUPPORTED_MESSENGERS.LINE]: 'LINE',
  [SUPPORTED_MESSENGERS.FACEBOOK_MESSENGER]: 'Facebook Messenger',
  [SUPPORTED_MESSENGERS.TELEGRAM]: 'Telegram',
  [SUPPORTED_MESSENGERS.INSTAGRAM]: 'Instagram',
};

export const ALL_PACKAGE_NAMES = Object.values(SUPPORTED_MESSENGERS);
