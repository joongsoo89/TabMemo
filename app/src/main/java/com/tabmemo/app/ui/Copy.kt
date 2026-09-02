package com.tabmemo.app.ui

import java.util.Locale

enum class Lang {
    Ko, Ja, En;

    companion object {
        fun fromSystem(): Lang {
            val tag = Locale.getDefault().language.lowercase(Locale.ROOT)
            return when {
                tag.startsWith("ja") -> Ja
                tag.startsWith("en") -> En
                else -> Ko
            }
        }
    }
}

data class Copy(
    val appName: String,
    val search: String,
    val emptyTitle: String,
    val emptyHint: String,
    val add: String,
    val edit: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val confirmDelete: String,
    val title: String,
    val titleHint: String,
    val mainMemo: String,
    val mainMemoShort: String,
    val mainPlaceholder: String,
    val tabPlaceholder: String,
    val noContent: String,
    val needTitle: String,
    val needTabTitle: String,
    val addTab: String,
    val addAction: String,
    val newTabTitle: String,
    val newTabHint: String,
    val deleteTab: String,
    val confirmDeleteTab: String,
    val tabCount: (Int) -> String,
    val saved: String,
)

fun stringsForLang(lang: Lang): Copy = when (lang) {
    Lang.Ko -> Copy(
        appName = "TabMemo",
        search = "메모 검색",
        emptyTitle = "아직 등록된 메모가 없어요",
        emptyHint = "대주제를 만들고, 그 안에 탭을 추가해 필요한 메모를 나눠 적어 보세요.",
        add = "메모 추가",
        edit = "편집",
        save = "저장",
        cancel = "취소",
        delete = "삭제",
        confirmDelete = "이 메모를 삭제할까요? 안의 탭 메모도 함께 삭제됩니다.",
        title = "대주제",
        titleHint = "예: 여행 준비, 이번 주 할 일",
        mainMemo = "메인 메모",
        mainMemoShort = "메인",
        mainPlaceholder = "이 주제의 핵심 메모를 적어 주세요.",
        tabPlaceholder = "이 탭에 적을 내용을 입력하세요.",
        noContent = "아직 작성된 내용이 없습니다.",
        needTitle = "대주제를 입력해 주세요.",
        needTabTitle = "탭 이름을 입력해 주세요.",
        addTab = "+",
        addAction = "추가",
        newTabTitle = "탭 이름",
        newTabHint = "예: 숙소, 맛집, 체크리스트",
        deleteTab = "탭 삭제",
        confirmDeleteTab = "이 탭을 삭제할까요?",
        tabCount = { count -> if (count == 0) "탭 없음" else "탭 ${count}개" },
        saved = "저장했습니다.",
    )
    Lang.Ja -> Copy(
        appName = "TabMemo",
        search = "メモを検索",
        emptyTitle = "まだメモがありません",
        emptyHint = "大きなテーマを作って、その中にタブを追加してメモを分けて書けます。",
        add = "メモを追加",
        edit = "編集",
        save = "保存",
        cancel = "キャンセル",
        delete = "削除",
        confirmDelete = "このメモを削除しますか？中のタブメモも一緒に削除されます。",
        title = "テーマ",
        titleHint = "例: 旅行の準備、今週やること",
        mainMemo = "メインメモ",
        mainMemoShort = "メイン",
        mainPlaceholder = "このテーマの中心になるメモを書いてください。",
        tabPlaceholder = "このタブに書く内容を入力してください。",
        noContent = "まだ内容がありません。",
        needTitle = "テーマを入力してください。",
        needTabTitle = "タブ名を入力してください。",
        addTab = "+",
        addAction = "追加",
        newTabTitle = "タブ名",
        newTabHint = "例: 宿、グルメ、チェックリスト",
        deleteTab = "タブを削除",
        confirmDeleteTab = "このタブを削除しますか？",
        tabCount = { count -> if (count == 0) "タブなし" else "タブ ${count}件" },
        saved = "保存しました。",
    )
    Lang.En -> Copy(
        appName = "TabMemo",
        search = "Search memos",
        emptyTitle = "No memos yet",
        emptyHint = "Create a main topic, then add tabs inside it for the notes you need.",
        add = "Add memo",
        edit = "Edit",
        save = "Save",
        cancel = "Cancel",
        delete = "Delete",
        confirmDelete = "Delete this memo? All tab notes inside it will be deleted too.",
        title = "Topic",
        titleHint = "e.g. Trip prep, This week",
        mainMemo = "Main memo",
        mainMemoShort = "Main",
        mainPlaceholder = "Write the core note for this topic.",
        tabPlaceholder = "Write the note for this tab.",
        noContent = "Nothing written here yet.",
        needTitle = "Please enter a topic.",
        needTabTitle = "Please enter a tab name.",
        addTab = "+",
        addAction = "Add",
        newTabTitle = "Tab name",
        newTabHint = "e.g. Stay, Food, Checklist",
        deleteTab = "Delete tab",
        confirmDeleteTab = "Delete this tab?",
        tabCount = { count -> if (count == 0) "No tabs" else "$count tab${if (count == 1) "" else "s"}" },
        saved = "Saved.",
    )
}
