package com.ihdyo.smarthome.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ihdyo.smarthome.R

class ModalBottomSheet : BottomSheetDialogFragment() {

    private var listener: BottomSheetListener? = null

    interface BottomSheetListener {
        fun onTextEntered(title: String, text: String)
    }

    companion object {
        private const val ARG_TITLE = "argTitle"
        private const val ARG_HINT = "argHint"
        private const val ARG_END_ICON_MODE = "argEndIconMode"
        private const val ARG_START_ICON_DRAWABLE = "argStartIconDrawable"
        private const val ARG_INPUT_TYPE = "argInputType"
        private const val ARG_BUTTON_TEXT = "argButtonText"

        fun newInstance(title: String, hint: String, endIconMode: Int, startIconDrawable: Int, inputType: Int, buttonText: String): ModalBottomSheet {
            val fragment = ModalBottomSheet()
            val args = Bundle()

            args.putString(ARG_TITLE, title)
            args.putString(ARG_HINT, hint)
            args.putInt(ARG_END_ICON_MODE, endIconMode)
            args.putInt(ARG_START_ICON_DRAWABLE, startIconDrawable)
            args.putInt(ARG_INPUT_TYPE, inputType)
            args.putString(ARG_BUTTON_TEXT, buttonText)

            fragment.arguments = args
            return fragment
        }
    }

    fun setListener(listener: BottomSheetListener) {
        this.listener = listener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        val title = arguments?.getString(ARG_TITLE)
        val hint = arguments?.getString(ARG_HINT)
        val endIconMode = arguments?.getInt(ARG_END_ICON_MODE)
        val startIconDrawable = arguments?.getInt(ARG_START_ICON_DRAWABLE)
        val inputType = arguments?.getInt(ARG_INPUT_TYPE)
        val buttonText = arguments?.getString(ARG_BUTTON_TEXT)

        // Set dynamic content
        val textTitle: TextView = view.findViewById(R.id.text_title)
        val textLayout: TextInputLayout = view.findViewById(R.id.text_layout)
        val inputText: TextInputEditText = view.findViewById(R.id.input_text)
        val buttonProcess: Button = view.findViewById(R.id.button_proccess)

        textTitle.text = title
        textLayout.hint = hint
        textLayout.endIconMode = endIconMode ?: TextInputLayout.END_ICON_NONE
//        textLayout.startIconDrawable = startIconDrawable ?: 0
        inputText.inputType = inputType ?: android.text.InputType.TYPE_CLASS_TEXT
        buttonProcess.text = buttonText

        // Proccess The Logic
        buttonProcess.setOnClickListener {
            Vibration.vibrate(requireContext())

            val enteredText = inputText.text.toString()
            listener?.onTextEntered(title.toString(), enteredText)
            dismiss()
        }
    }
}
