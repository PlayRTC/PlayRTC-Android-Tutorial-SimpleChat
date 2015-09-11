![gradle build](https://travis-ci.org/PlayRTC/PlayRTC-Android-Tutorial-SimpleChat.svg)

# PlayRTC Android Tutorial SimpleChat
PlayRTC의 안드로이드 튜토리얼 SimpleChat

## 개요
이 저장소는 PlayRTC의 안드로이드 SDK를 사용하는데 있어서 가장 기본적인 내용을 담고 있습니다.
이 예제를 통해 PlayRTC의 가장 기본 적인 예제인 화상 채팅 앱을 만들 수 있으며, 직접 사용해 볼 수 있습니다.

## 로그
SimpleChat앱은 테스트를 위한 용도로 로그를 SD카드에 저장하도록 되어있습니다.
저장 위치는 아래와 같습니다.

- `/Android/data/com.playrtc.simplechat/files/log`

## 참고

### 버전
- 버전은 [유의적 버전 2.0.0](http://semver.org/lang/ko/)를 따르도록 함
- 단 `x`.y.z 형태에서 주 버전인 `x`는 PlayRTC Android SDK Version을 그대로 따르도록 함

### 환경
- Build : Gradle
- IDE : Android Studio
- Deploy : [TravisCI](https://travis-ci.org/PlayRTC/PlayRTC-Android-Tutorial-SimpleChat)

### 배포
- 특정 Git 커밋에 버전 태그를 삽입
- Github 저장소에 Push
- Travis를 통해 apk 파일로 빌드
- Github의 Release에 apk 파일 업로드

## 갱신 기록

### 2.0.4
- SDK : 2.0.4

### 2.0.3
- SDK : 2.0.3
- 신규 : PlayRTCAudioManager 추가
- 변경 : PlayRTC 인스턴스 생성 방법 변경, PlayRTCSettings을 인자로 전달
  - newInstance(PlayRTCSettings settings, PlayRTCObserver observer)
        
### 2.0.2-rc.3
- SDK : 2.0.1-rc.3

### 2.0.2-rc.2
- SDK : 2.0.1-rc.2

### 2.0.2-rc.1
- SDK : 2.0.1-rc.1

### 2.0.1
- SDK : 2.0.0
- 신규 : 아이콘
- 변경 : 백버튼 누를 시 앱 종료가 안되던것 수정

### 2.0.0
- SDK : 2.0.0
- 신규 : PlayRTC Android SDK Version 2.0.0에 맞추어 첫 배포

COPYRIGHT (c) 2015 SK TELECOM CO. LTD. ALL RIGHTS RESERVED.
