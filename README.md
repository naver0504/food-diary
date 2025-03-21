# 식사일기

![Untitled](https://github.com/user-attachments/assets/7399e0f8-5f18-45f3-92b1-541ac5256b22)


### **Back-End Developer**                        
---

- 개발 : 2023.07 ~ 2024.03
- 개발 인원 : 백엔드 2, 프론트엔드 1, 기획 및 디자인 1

### 🛠 기술 스택

---

- Spring, Spring Boot, Spring Data JPA, MySQL
- Git, Docker, AWS EC2, S3

### 📖 서비스 내용 https://myfooddiary.imweb.me/

---

- 클릭 3번으로 사진과 함께 식사일기 빠르게 등록
- 월별 캘린더/ 타임라인으로 그동안 올렸던 식사일기를 한눈에 확인
- 먹었던 음식에 대해 간단히 식사분류, 메모, 장소, #태그 가능
- 검색 기능으로 한 번에 사용자가 원하는 식사 일기를 찾기

### 🙋‍♂️ 역할

---

1. **이미지 처리 및 S3 연동**
사용자가 업로드한 식사일기 이미지의 확장자 추출, 썸네일 생성 등 이미지 유틸 클래스를 구현하고, S3를 활용한 이미지 업로드 및 다운로드 기능을 구현했습니다.
    - 한 화면에 **최대 31개**의 이미지를 전송해야 하는 상황에서, **네트워크 트래픽**을 줄이고 **빠른 응답 속도**를 확보하기 위해 생성한 썸네일을 **S3**에 저장했습니다. 그 과정에서 **이미지 용량을 최대한 줄이는 동시에**, 사용자가 이미지를 쉽게 식별할 수 있는 **적절한 해상도**(가로·세로 크기)를 찾았습니다.
    - **Try-With-Resources** 전략을 활용하여 코드를 **간결화하고** 자원을 **누락 없이** 안전하게 반환하도록 구현했습니다.
2. **검색 기능 구현**
식사 시간, 장소, #태그 등을 카테고리로 분류하고, 사용자가 입력한 대소문자에 따라 검색 결과를  정렬해 조회하는 기능을 구현했습니다.
    - **대소문자를 구분**하여 조회할 때 처음에는 단순히 `ORDER BY`로 쉽게 해결할 수 있을 것이라 생각했으나, MySQL에서 `VARCHAR` 타입 문자열 검색이 기본적으로 대소문자를 구분하지 않는다는 점에서 문제가 발생했습니다. 이를 해결하기 위해 MySQL의 `BINARY` 키워드를 사용하여 **대소문자를 구분**하도록 처리했습니다.
3. **식사일기 데이터 처리**
업로드된 식사일기에 따라 식사 시간을 분류하고, MySQL에 장소, 위도, 경도 등의 위치 정보를 저장하는 로직을 구현했습니다.
    - **특정 날짜의 일기**를 조회하기 위해, 해당 날짜의 **마지막 시각**을 설정하려고 다음 날 **00:00:00**에서 1 **나노초**를 빼는 방식으로 코드를 구현했으나, MySQL `DATETIME`의 **fractional seconds**로 인해 **반올림되어** 의도치 않게 **다음 날까지** 조회되는 문제가 발생했습니다. 이를 해결하기 위해 [MySQL 공식 문서](https://dev.mysql.com/doc/refman/5.7/en/fractional-seconds.html)를 참고해 수정했습니다.

### 🧐 느낀 점

---

이 프로젝트를 진행하며 기본의 중요성에 대해 다시 한번 깨달았습니다. 이전에는 고민 없이  문자열은 `VARCHAR` 타입으로 설정하는 방식으로 작업해왔습니다. 하지만 이번 프로젝트에서 발생한 문제를 해결하는 과정에서 데이터 타입 선택에 대해 더 신중히 접근해야 한다는 것을 느꼈습니다.

특히, 대소문자 구분 처리와 관련된 문제를 조사하면서 `CHAR*와 `VARCHAR`의 차이에 대해 자세 이해하게 되었고, 주민등록번호와 같이 길이가 고정되고 자주 변경되지 않는 데이터를 저장할 때는 `VARCHAR`가 아닌 `CHAR` 타입을 사용하는 것이 더 적합하다는 점을 배우게 되었습니다. 

### 👀 서비스 화면

---

![Untitled (1)](https://github.com/user-attachments/assets/82b3e249-bbe6-44b6-a6fc-a7257d59fb7b)
